package com.ptithcm.smartshop.cart.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ptithcm.smartshop.address.dto.AddressFormDTO;
import com.ptithcm.smartshop.address.dto.AddressViewDTO;
import com.ptithcm.smartshop.address.service.AddressService;
import com.ptithcm.smartshop.cart.service.CartService;
import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.dto.cart.CartShippingAddressDTO;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import com.ptithcm.smartshop.voucher.dto.VoucherListItemDTO;
import com.ptithcm.smartshop.voucher.entity.VoucherScope;
import com.ptithcm.smartshop.voucher.service.VoucherService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/cart")
public class CartWebController {

    private final CartService cartService;
    private final AddressService addressService;
    private final VoucherService voucherService;

    public CartWebController(CartService cartService, AddressService addressService, VoucherService voucherService) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.voucherService = voucherService;
    }

    @GetMapping
    public String viewCart(@RequestParam(required = false) Boolean manageAddress,
                           @RequestParam(required = false) UUID editAddressId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("formError", "Vui lòng đăng nhập để sử dụng giỏ hàng");
            return "redirect:/auth/login";
        }

        CartDTO cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("currentCartUrl", buildCartUrl(manageAddress, editAddressId));

        boolean showAddressManager = Boolean.TRUE.equals(manageAddress);
        List<AddressViewDTO> savedAddresses = addressService.findByUserId(sessionUser.id());
        UUID selectedAddressId = resolveSelectedAddressId(session, sessionUser.id(), savedAddresses);
        Optional<AddressViewDTO> selectedAddress = savedAddresses.stream()
                .filter(address -> address.getId().equals(selectedAddressId))
                .findFirst();

        selectedAddress.ifPresent(address -> {
            session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID, address.getId().toString());
            session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS, address.toShippingAddress());
        });

        model.addAttribute("hasLoggedInUser", true);
        model.addAttribute("showAddressManager", showAddressManager);
        model.addAttribute("savedAddresses", savedAddresses);
        model.addAttribute("selectedAddressId", selectedAddressId);
        model.addAttribute("selectedAddress", selectedAddress.orElse(null));
        model.addAttribute("addressForm", buildAddressForm(editAddressId, sessionUser.id(), sessionUser));
        model.addAttribute("isAddressEdit", editAddressId != null);

        return "cart/index";
    }

    @PostMapping("/address")
    public String saveShippingAddress(@Valid @ModelAttribute("shippingAddress") CartShippingAddressDTO shippingAddress,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            FieldError firstError = bindingResult.getFieldErrors().stream().findFirst().orElse(null);
            redirectAttributes.addFlashAttribute("addressErrorMessage",
                    firstError != null ? firstError.getDefaultMessage() : "Thông tin địa chỉ chưa hợp lệ");
            return "redirect:/cart";
        }

        session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS, shippingAddress);
        redirectAttributes.addFlashAttribute("addressSuccessMessage", "Đã lưu địa chỉ giao hàng");
        return "redirect:/cart";
    }

    @GetMapping("/vouchers")
    @ResponseBody
    public List<VoucherListItemDTO> listVouchers(HttpSession session) {
        CartDTO cart = cartService.getCart(session);
        return voucherService.getVouchersForCart(session, cart);
    }

    @PostMapping("/voucher/apply")
    public String applyVoucher(@RequestParam String voucherCode,
                               @RequestParam VoucherScope scope,
                               @RequestParam(required = false) String redirectUrl,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            cartService.applyVoucher(session, voucherCode, scope);
            redirectAttributes.addFlashAttribute("voucherSuccessMessage",
                    scope == VoucherScope.ORDER ? "Đã áp dụng mã giảm giá đơn hàng" : "Đã áp dụng mã miễn phí vận chuyển");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("voucherErrorMessage", exception.getMessage());
        }
        return buildRedirectResponse(redirectUrl);
    }

    @PostMapping("/voucher/remove")
    public String removeVoucher(@RequestParam VoucherScope scope,
                                @RequestParam(required = false) String redirectUrl,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        cartService.removeVoucher(session, scope);
        redirectAttributes.addFlashAttribute("voucherSuccessMessage",
                scope == VoucherScope.ORDER ? "Đã gỡ voucher đơn hàng" : "Đã gỡ voucher vận chuyển");
        return buildRedirectResponse(redirectUrl);
    }

    @PostMapping("/address/save")
    public String saveUserAddress(@Valid @ModelAttribute("addressForm") AddressFormDTO addressForm,
                                  BindingResult bindingResult,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("addressErrorMessage", "Vui lòng đăng nhập để lưu địa chỉ");
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            FieldError firstError = bindingResult.getFieldErrors().stream().findFirst().orElse(null);
            redirectAttributes.addFlashAttribute("addressErrorMessage",
                    firstError != null ? firstError.getDefaultMessage() : "Thông tin địa chỉ chưa hợp lệ");
            return "redirect:/cart?manageAddress=true";
        }

        boolean editing = addressForm.getId() != null;
        AddressViewDTO savedAddress = addressService.save(sessionUser.id(), addressForm);
        session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID, savedAddress.getId().toString());
        session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS, savedAddress.toShippingAddress());

        redirectAttributes.addFlashAttribute("addressSuccessMessage", editing ? "Đã cập nhật địa chỉ" : "Đã thêm địa chỉ mới");
        return "redirect:/cart?manageAddress=true";
    }

    @PostMapping("/address/select")
    public String selectSavedAddress(@RequestParam UUID addressId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("addressErrorMessage", "Vui lòng đăng nhập để chọn địa chỉ");
            return "redirect:/auth/login";
        }

        Optional<AddressViewDTO> selectedAddress = addressService.findByIdAndUserId(addressId, sessionUser.id());
        if (selectedAddress.isEmpty()) {
            redirectAttributes.addFlashAttribute("addressErrorMessage", "Không tìm thấy địa chỉ cần chọn");
            return "redirect:/cart?manageAddress=true";
        }

        session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID, addressId.toString());
        session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS, selectedAddress.get().toShippingAddress());
        redirectAttributes.addFlashAttribute("addressSuccessMessage", "Đã chọn địa chỉ giao hàng");
        return "redirect:/cart";
    }

    @PostMapping("/address/delete")
    public String deleteSavedAddress(@RequestParam UUID addressId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("addressErrorMessage", "Vui lòng đăng nhập để xóa địa chỉ");
            return "redirect:/auth/login";
        }

        addressService.delete(sessionUser.id(), addressId);

        Object selectedAddressId = session.getAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID);
        if (selectedAddressId instanceof String selectedAddressIdText) {
            try {
                UUID selectedId = UUID.fromString(selectedAddressIdText);
                if (addressId.equals(selectedId)) {
                    session.removeAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID);
                    session.removeAttribute(CartSessionConstants.SHIPPING_ADDRESS);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        redirectAttributes.addFlashAttribute("addressSuccessMessage", "Đã xóa địa chỉ");
        return "redirect:/cart?manageAddress=true";
    }

    @PostMapping("/address/clear")
    public String clearShippingAddress(HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser != null) {
            Optional<AddressViewDTO> defaultAddress = addressService.findDefaultByUserId(sessionUser.id());
            if (defaultAddress.isPresent()) {
                session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID, defaultAddress.get().getId().toString());
                session.setAttribute(CartSessionConstants.SHIPPING_ADDRESS, defaultAddress.get().toShippingAddress());
                redirectAttributes.addFlashAttribute("addressSuccessMessage", "Đã chuyển về địa chỉ mặc định");
                return "redirect:/cart";
            }
            session.removeAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID);
        }

        session.removeAttribute(CartSessionConstants.SHIPPING_ADDRESS);
        redirectAttributes.addFlashAttribute("addressSuccessMessage", "Đã xóa địa chỉ giao hàng đã lưu");
        return "redirect:/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam String productId,
                            @RequestParam(required = false) String variantId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            @RequestParam(required = false) String redirectUrl,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (resolveCurrentUser(session) == null) {
            boolean buyNowFlow = redirectUrl != null && redirectUrl.startsWith("/checkout");
            redirectAttributes.addFlashAttribute("formError",
                    buyNowFlow
                            ? "Vui lòng đăng nhập để mua ngay"
                            : "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
            return "redirect:/auth/login";
        }

        try {
            cartService.addToCart(session, productId, variantId, quantity);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartErrorMessage", exception.getMessage());
            return buildRedirectResponse(redirectUrl);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng");
        return buildRedirectResponse(redirectUrl);
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam String productId,
                                 @RequestParam(required = false) String variantId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (resolveCurrentUser(session) == null) {
            redirectAttributes.addFlashAttribute("formError", "Vui lòng đăng nhập để sử dụng giỏ hàng");
            return "redirect:/auth/login";
        }

        try {
            cartService.updateQuantity(session, productId, variantId, quantity);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartErrorMessage", exception.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam String productId,
                             @RequestParam(required = false) String variantId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (resolveCurrentUser(session) == null) {
            redirectAttributes.addFlashAttribute("formError", "Vui lòng đăng nhập để sử dụng giỏ hàng");
            return "redirect:/auth/login";
        }
        cartService.removeItem(session, productId, variantId);
        return "redirect:/cart";
    }

    private AddressFormDTO buildAddressForm(UUID editAddressId, UUID userId, SessionUser sessionUser) {
        if (editAddressId != null) {
            Optional<AddressViewDTO> editAddress = addressService.findByIdAndUserId(editAddressId, userId);
            if (editAddress.isPresent()) {
                return addressService.toForm(editAddress.get());
            }
        }

        AddressFormDTO form = new AddressFormDTO();
        form.setReceiverName(sessionUser.fullName());
        form.setPhone(sessionUser.phone());
        return form;
    }

    private UUID resolveSelectedAddressId(HttpSession session, UUID userId, List<AddressViewDTO> savedAddresses) {
        Object selectedIdFromSession = session.getAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID);
        if (selectedIdFromSession instanceof String selectedIdText) {
            try {
                UUID selectedId = UUID.fromString(selectedIdText);
                boolean exists = savedAddresses.stream().anyMatch(address -> address.getId().equals(selectedId));
                if (exists) {
                    return selectedId;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        Optional<AddressViewDTO> defaultAddress = addressService.findDefaultByUserId(userId);
        if (defaultAddress.isPresent()) {
            return defaultAddress.get().getId();
        }

        if (!savedAddresses.isEmpty()) {
            return savedAddresses.get(0).getId();
        }
        return null;
    }

    private SessionUser resolveCurrentUser(HttpSession session) {
        Object currentUser = session.getAttribute(SessionConstants.CURRENT_USER);
        if (currentUser instanceof SessionUser sessionUser) {
            return sessionUser;
        }
        return null;
    }

    private String buildCartUrl(Boolean manageAddress, UUID editAddressId) {
        if (editAddressId != null) {
            return "/cart?manageAddress=true&editAddressId=" + editAddressId;
        }
        if (Boolean.TRUE.equals(manageAddress)) {
            return "/cart?manageAddress=true";
        }
        return "/cart";
    }

    private String buildRedirectResponse(String redirectUrl) {
        if (redirectUrl != null && !redirectUrl.isBlank() && redirectUrl.startsWith("/")) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/cart";
    }

}