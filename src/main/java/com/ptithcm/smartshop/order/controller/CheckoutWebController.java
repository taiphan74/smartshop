package com.ptithcm.smartshop.order.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ptithcm.smartshop.address.dto.AddressViewDTO;
import com.ptithcm.smartshop.address.service.AddressService;
import com.ptithcm.smartshop.cart.controller.CartSessionConstants;
import com.ptithcm.smartshop.cart.service.CartService;
import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.dto.cart.CartShippingAddressDTO;
import com.ptithcm.smartshop.order.dto.request.CheckoutRequest;
import com.ptithcm.smartshop.order.service.CheckoutService;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/checkout")
public class CheckoutWebController {

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final AddressService addressService;

    public CheckoutWebController(CartService cartService,
                                 CheckoutService checkoutService,
                                 AddressService addressService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.addressService = addressService;
    }

    @GetMapping
    public String viewCheckout(HttpSession session, Model model) {
        CartDTO cart = cartService.getCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        if (!model.containsAttribute("checkoutRequest")) {
            model.addAttribute("checkoutRequest", buildPrefilledRequest(session));
        }
        CheckoutRequest checkoutRequest = (CheckoutRequest) model.getAttribute("checkoutRequest");
        model.addAttribute("showAddressSelectors", shouldShowAddressSelectors(checkoutRequest));
        return "checkout/index";
    }

    @PostMapping("/process")
    public String processCheckout(@Valid @ModelAttribute("checkoutRequest") CheckoutRequest checkoutRequest,
                                  BindingResult bindingResult,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        CartDTO cart = cartService.getCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        composeAddressIfStructured(checkoutRequest);
        validateAddress(checkoutRequest, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart);
            model.addAttribute("showAddressSelectors", shouldShowAddressSelectors(checkoutRequest));
            return "checkout/index";
        }

        String orderCode = checkoutService.placeOrder(session, checkoutRequest);
        redirectAttributes.addFlashAttribute("orderCode", orderCode);
        return "redirect:/checkout/success";
    }

    @GetMapping("/success")
    public String checkoutSuccess(Model model) {
        return "checkout/success";
    }

    private CheckoutRequest buildPrefilledRequest(HttpSession session) {
        CheckoutRequest request = new CheckoutRequest();

        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser != null) {
            AddressViewDTO selectedAddress = resolveSelectedUserAddress(session, sessionUser.id());
            if (selectedAddress != null) {
                request.setCustomerName(selectedAddress.getReceiverName());
                request.setCustomerPhone(selectedAddress.getPhone());
                request.setProvince(selectedAddress.getProvince());
                request.setDistrict(selectedAddress.getDistrict());
                request.setWard(selectedAddress.getWard());
                request.setDetailAddress(selectedAddress.getDetail());
                request.setCustomerAddress(selectedAddress.getFullAddress());
                return request;
            }
        }

        Object savedAddress = session.getAttribute(CartSessionConstants.SHIPPING_ADDRESS);
        if (savedAddress instanceof CartShippingAddressDTO address && address.isComplete()) {
            request.setCustomerName(address.getRecipientName());
            request.setCustomerPhone(address.getPhone());
            request.setProvince(address.getProvince());
            request.setDistrict(address.getDistrict());
            request.setWard(address.getWard());
            request.setDetailAddress(address.getDetailAddress());
            request.setCustomerAddress(address.getFullAddress());
        }
        return request;
    }

    private boolean shouldShowAddressSelectors(CheckoutRequest request) {
        return request == null || !hasStructuredAddress(request);
    }

    private boolean hasStructuredAddress(CheckoutRequest request) {
        return hasText(request.getProvince())
                && hasText(request.getDistrict())
                && hasText(request.getWard())
                && hasText(request.getDetailAddress());
    }

    private void composeAddressIfStructured(CheckoutRequest request) {
        if (!hasStructuredAddress(request)) {
            return;
        }
        request.setCustomerAddress(String.join(", ",
                request.getDetailAddress(),
                request.getWard(),
                request.getDistrict(),
                request.getProvince()));
    }

    private void validateAddress(CheckoutRequest request, BindingResult bindingResult) {
        if (!hasText(request.getCustomerAddress())) {
            bindingResult.rejectValue("province", "checkout.province.required", "Vui lòng chọn tỉnh/thành phố");
            bindingResult.rejectValue("district", "checkout.district.required", "Vui lòng chọn quận/huyện");
            bindingResult.rejectValue("ward", "checkout.ward.required", "Vui lòng chọn phường/xã");
            bindingResult.rejectValue("detailAddress", "checkout.detailAddress.required", "Vui lòng nhập số nhà, tên đường");
            return;
        }

        if (hasText(request.getProvince()) || hasText(request.getDistrict()) || hasText(request.getWard()) || hasText(request.getDetailAddress())) {
            if (!hasText(request.getProvince())) {
                bindingResult.rejectValue("province", "checkout.province.required", "Vui lòng chọn tỉnh/thành phố");
            }
            if (!hasText(request.getDistrict())) {
                bindingResult.rejectValue("district", "checkout.district.required", "Vui lòng chọn quận/huyện");
            }
            if (!hasText(request.getWard())) {
                bindingResult.rejectValue("ward", "checkout.ward.required", "Vui lòng chọn phường/xã");
            }
            if (!hasText(request.getDetailAddress())) {
                bindingResult.rejectValue("detailAddress", "checkout.detailAddress.required", "Vui lòng nhập số nhà, tên đường");
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private AddressViewDTO resolveSelectedUserAddress(HttpSession session, UUID userId) {
        Object selectedAddressId = session.getAttribute(CartSessionConstants.SHIPPING_ADDRESS_ID);
        if (selectedAddressId instanceof String selectedAddressIdText) {
            try {
                UUID addressId = UUID.fromString(selectedAddressIdText);
                Optional<AddressViewDTO> selected = addressService.findByIdAndUserId(addressId, userId);
                if (selected.isPresent()) {
                    return selected.get();
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return addressService.findDefaultByUserId(userId).orElse(null);
    }

    private SessionUser resolveCurrentUser(HttpSession session) {
        Object currentUser = session.getAttribute(SessionConstants.CURRENT_USER);
        if (currentUser instanceof SessionUser sessionUser) {
            return sessionUser;
        }
        return null;
    }
}