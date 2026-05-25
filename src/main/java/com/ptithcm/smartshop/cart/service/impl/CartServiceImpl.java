package com.ptithcm.smartshop.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptithcm.smartshop.cart.entity.SessionCart;
import com.ptithcm.smartshop.cart.entity.SessionCartItem;
import com.ptithcm.smartshop.cart.repository.SessionCartItemRepository;
import com.ptithcm.smartshop.cart.repository.SessionCartRepository;
import com.ptithcm.smartshop.cart.service.CartService;
import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.dto.cart.CartItemDTO;
import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.ProductVariantDTO;
import com.ptithcm.smartshop.product.service.ProductService;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import com.ptithcm.smartshop.voucher.entity.VoucherScope;
import com.ptithcm.smartshop.voucher.service.VoucherService;

import jakarta.servlet.http.HttpSession;

@Service
public class CartServiceImpl implements CartService {

    private final SessionCartRepository cartRepository;
    private final SessionCartItemRepository cartItemRepository;
    private final ProductService productService;
    private final VoucherService voucherService;

    public CartServiceImpl(
            SessionCartRepository cartRepository,
            SessionCartItemRepository cartItemRepository,
            ProductService productService,
            VoucherService voucherService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.voucherService = voucherService;
    }

    private SessionCart getDbCart(HttpSession session) {
        String sessionId = session.getId();
        SessionUser sessionUser = resolveCurrentUser(session);

        if (sessionUser == null) {
            return cartRepository.findBySessionId(sessionId).orElseGet(() -> {
                SessionCart cart = new SessionCart();
                cart.setSessionId(sessionId);
                return cartRepository.save(cart);
            });
        }

        UUID userId = sessionUser.id();
        Optional<SessionCart> userCartOpt = cartRepository.findByUserId(userId);
        Optional<SessionCart> sessionCartOpt = cartRepository.findBySessionId(sessionId);

        if (userCartOpt.isPresent()) {
            SessionCart userCart = userCartOpt.get();

            if (sessionCartOpt.isPresent() && !sessionCartOpt.get().getId().equals(userCart.getId())) {
                mergeCarts(userCart, sessionCartOpt.get());
                cartRepository.delete(sessionCartOpt.get());
            }

            userCart.setSessionId(sessionId);
            userCart.setUserId(userId);
            return cartRepository.save(userCart);
        }

        if (sessionCartOpt.isPresent()) {
            SessionCart sessionCart = sessionCartOpt.get();
            sessionCart.setUserId(userId);
            sessionCart.setSessionId(sessionId);
            return cartRepository.save(sessionCart);
        }

        SessionCart cart = new SessionCart();
        cart.setSessionId(sessionId);
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartDTO getCart(HttpSession session) {
        SessionCart cart = getDbCart(session);
        return mapCart(cart, session);
    }

    @Override
    @Transactional
    public int getCartCount(HttpSession session) {
        return mapCart(getDbCart(session), null).getTotalQuantity();
    }

    private void mergeCarts(SessionCart target, SessionCart source) {
        for (SessionCartItem sourceItem : source.getItems()) {
            Optional<SessionCartItem> existingItem = target.getItems().stream()
                    .filter(item -> item.getProductId().equals(sourceItem.getProductId())
                            && sameVariant(item.getVariantId(), sourceItem.getVariantId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                SessionCartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + sourceItem.getQuantity());
                cartItemRepository.save(item);
            } else {
                SessionCartItem copiedItem = new SessionCartItem();
                copiedItem.setCart(target);
                copiedItem.setProductId(sourceItem.getProductId());
                copiedItem.setVariantId(sourceItem.getVariantId());
                copiedItem.setProductName(sourceItem.getProductName());
                copiedItem.setImageUrl(sourceItem.getImageUrl());
                copiedItem.setPrice(sourceItem.getPrice());
                copiedItem.setQuantity(sourceItem.getQuantity());
                target.getItems().add(copiedItem);
                cartItemRepository.save(copiedItem);
            }
        }

        cartItemRepository.deleteAll(new ArrayList<>(source.getItems()));
        source.getItems().clear();
    }

    private boolean sameVariant(String leftVariantId, String rightVariantId) {
        return leftVariantId == null ? rightVariantId == null : leftVariantId.equals(rightVariantId);
    }

    private SessionUser resolveCurrentUser(HttpSession session) {
        Object currentUser = session.getAttribute(SessionConstants.CURRENT_USER);
        if (currentUser instanceof SessionUser sessionUser) {
            return sessionUser;
        }
        return null;
    }

    private CartDTO mapCart(SessionCart cart, HttpSession session) {
        CartDTO dto = new CartDTO();
        for (SessionCartItem item : cart.getItems()) {
            CartItemDTO itemDto = new CartItemDTO();
            itemDto.setProductId(item.getProductId());
            itemDto.setVariantId(item.getVariantId());
            itemDto.setProductName(item.getProductName());
            itemDto.setImageUrl(item.getImageUrl());
            itemDto.setPrice(item.getPrice());
            itemDto.setQuantity(item.getQuantity());
            dto.getItems().add(itemDto);
        }
        recalculateCart(dto);
        if (session != null) {
            voucherService.applySessionVouchers(session, dto);
        }
        return dto;
    }

    @Override
    @Transactional
    public void addToCart(HttpSession session, String productId, String variantId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng thêm vào giỏ hàng phải lớn hơn 0");
        }

        ProductDetailDTO product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        SessionCart cart = getDbCart(session);

        String finalVariantId = variantId != null && variantId.trim().isEmpty() ? null : variantId;
        Optional<ProductVariantDTO> selectedVariant = resolveVariant(product, finalVariantId);
        int stockQuantity = resolveStockQuantity(product, selectedVariant);
        if (stockQuantity <= 0) {
            throw new IllegalArgumentException("Sản phẩm đã hết hàng");
        }

        Optional<SessionCartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)
                        && (finalVariantId == null ? item.getVariantId() == null : finalVariantId.equals(item.getVariantId())))
                .findFirst();

        int existingQuantity = existingItemOpt.map(SessionCartItem::getQuantity).orElse(0);
        int targetQuantity = existingQuantity + quantity;
        if (targetQuantity > stockQuantity) {
            throw new IllegalArgumentException("Chỉ còn " + stockQuantity + " sản phẩm trong kho");
        }

        if (existingItemOpt.isPresent()) {
            SessionCartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            SessionCartItem newItem = new SessionCartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setVariantId(finalVariantId);
            newItem.setProductName(product.getName());

            String imageUrl = "https://via.placeholder.com/400";
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                imageUrl = product.getImages().get(0).getImageUrl();
            }

            BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            if (selectedVariant.isPresent()) {
                ProductVariantDTO variant = selectedVariant.get();
                if (variant.getPrice() != null) {
                    price = variant.getPrice();
                }
                if (variant.getThumbnailUrl() != null) {
                    imageUrl = variant.getThumbnailUrl();
                }
            }

            newItem.setImageUrl(imageUrl);
            newItem.setPrice(price);
            newItem.setQuantity(quantity);

            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateQuantity(HttpSession session, String productId, String variantId, Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ");
        }

        SessionCart cart = getDbCart(session);

        String finalVariantId = variantId != null && variantId.trim().isEmpty() ? null : variantId;

        Optional<SessionCartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)
                        && (finalVariantId == null ? item.getVariantId() == null : finalVariantId.equals(item.getVariantId())))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            SessionCartItem item = existingItemOpt.get();
            if (quantity <= 0) {
                cart.getItems().remove(item);
                cartItemRepository.delete(item);
            } else {
                ProductDetailDTO product = productService.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
                Optional<ProductVariantDTO> selectedVariant = resolveVariant(product, finalVariantId);
                int stockQuantity = resolveStockQuantity(product, selectedVariant);
                if (stockQuantity <= 0) {
                    throw new IllegalArgumentException("Sản phẩm đã hết hàng");
                }
                if (quantity > stockQuantity) {
                    throw new IllegalArgumentException("Chỉ còn " + stockQuantity + " sản phẩm trong kho");
                }
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
            cartRepository.save(cart);
        }
    }

    private Optional<ProductVariantDTO> resolveVariant(ProductDetailDTO product, String variantId) {
        if (variantId == null) {
            return Optional.empty();
        }
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Phân loại sản phẩm không hợp lệ");
        }
        ProductVariantDTO variant = product.getVariants().stream()
            .filter(item -> variantId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Phân loại sản phẩm không hợp lệ"));
        return Optional.of(variant);
    }

    private int resolveStockQuantity(ProductDetailDTO product, Optional<ProductVariantDTO> variantOpt) {
        Integer stock = variantOpt.map(ProductVariantDTO::getStockQuantity).orElse(product.getStockQuantity());
        return stock != null ? Math.max(stock, 0) : 0;
    }

    @Override
    @Transactional
    public void removeItem(HttpSession session, String productId, String variantId) {
        SessionCart cart = getDbCart(session);

        String finalVariantId = variantId != null && variantId.trim().isEmpty() ? null : variantId;

        Optional<SessionCartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)
                        && (finalVariantId == null ? item.getVariantId() == null : finalVariantId.equals(item.getVariantId())))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            SessionCartItem item = existingItemOpt.get();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void applyVoucher(HttpSession session, String voucherCode, VoucherScope scope) {
        CartDTO cart = getCart(session);
        voucherService.applyVoucher(session, cart, voucherCode, scope);
    }

    @Override
    @Transactional(readOnly = true)
    public void removeVoucher(HttpSession session, VoucherScope scope) {
        voucherService.removeVoucher(session, scope);
    }

    @Override
    @Transactional
    public void clearCart(HttpSession session) {
        SessionCart cart = getDbCart(session);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
        voucherService.clearAppliedVouchers(session);
    }

    private void recalculateCart(CartDTO cart) {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartItemDTO item : cart.getItems()) {
            subTotal = subTotal.add(item.getLineTotal());
        }
        cart.setSubTotal(subTotal);

        if (cart.getItems().isEmpty()) {
            cart.setShippingFee(BigDecimal.ZERO);
            cart.setTotalBeforeDiscount(BigDecimal.ZERO);
            cart.setTotal(BigDecimal.ZERO);
        } else {
            cart.setShippingFee(new BigDecimal("35000"));
            cart.setTotalBeforeDiscount(subTotal.add(cart.getShippingFee()));
            cart.setTotal(cart.getTotalBeforeDiscount());
        }

        if (cart.getItems().isEmpty()) {
            cart.setAppliedOrderVoucherCode(null);
            cart.setAppliedOrderVoucherName(null);
            cart.setAppliedShippingVoucherCode(null);
            cart.setAppliedShippingVoucherName(null);
            cart.setOrderDiscountAmount(BigDecimal.ZERO);
            cart.setShippingDiscountAmount(BigDecimal.ZERO);
        }
    }
}