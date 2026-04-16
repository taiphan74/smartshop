package com.ptithcm.smartshop.product.service.impl;

import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.ProductVariantDTO;
import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.dto.cart.CartItemDTO;
import com.ptithcm.smartshop.product.entity.Cart;
import com.ptithcm.smartshop.product.entity.CartItem;
import com.ptithcm.smartshop.product.repository.CartItemRepository;
import com.ptithcm.smartshop.product.repository.CartRepository;
import com.ptithcm.smartshop.product.service.CartService;
import com.ptithcm.smartshop.product.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    private Cart getDbCart(HttpSession session) {
        String sessionId = session.getId();
        return cartRepository.findBySessionId(sessionId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setSessionId(sessionId);
            return cartRepository.save(cart);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCart(HttpSession session) {
        Cart cart = getDbCart(session);
        CartDTO dto = new CartDTO();
        for (CartItem item : cart.getItems()) {
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
        return dto;
    }

    @Override
    @Transactional
    public void addToCart(HttpSession session, String productId, String variantId, Integer quantity) {
        Cart cart = getDbCart(session);
        
        final String finalVariantId = (variantId != null && variantId.trim().isEmpty()) ? null : variantId;

        // Find existing item
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId) && 
                               (finalVariantId == null ? item.getVariantId() == null : finalVariantId.equals(item.getVariantId())))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            // Need to fetch details from ProductService
            Optional<ProductDetailDTO> productOpt = productService.findById(productId);
            if (productOpt.isPresent()) {
                ProductDetailDTO product = productOpt.get();
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProductId(productId);
                newItem.setVariantId(finalVariantId);
                newItem.setProductName(product.getName());
                
                // Get image
                String imageUrl = "https://via.placeholder.com/400";
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    imageUrl = product.getImages().get(0).getImageUrl();
                }
                
                // Set price based on variant or base product
                BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                if (finalVariantId != null && product.getVariants() != null) {
                    Optional<ProductVariantDTO> variantOpt = product.getVariants().stream()
                            .filter(v -> finalVariantId.equals(v.getId()))
                            .findFirst();
                    if (variantOpt.isPresent() && variantOpt.get().getPrice() != null) {
                        price = variantOpt.get().getPrice();
                        if (variantOpt.get().getThumbnailUrl() != null) {
                            imageUrl = variantOpt.get().getThumbnailUrl();
                        }
                    }
                }
                
                newItem.setImageUrl(imageUrl);
                newItem.setPrice(price);
                newItem.setQuantity(quantity);
                
                cart.getItems().add(newItem);
                cartItemRepository.save(newItem);
            }
        }
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateQuantity(HttpSession session, String productId, String variantId, Integer quantity) {
        Cart cart = getDbCart(session);
        
        final String finalVariantId = (variantId != null && variantId.trim().isEmpty()) ? null : variantId;
        
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId) && 
                               (finalVariantId == null ? item.getVariantId() == null : finalVariantId.equals(item.getVariantId())))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            if (quantity <= 0) {
                cart.getItems().remove(item);
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public void removeItem(HttpSession session, String productId, String variantId) {
        Cart cart = getDbCart(session);
        
        final String finalVariantId = (variantId != null && variantId.trim().isEmpty()) ? null : variantId;

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId) && 
                               (finalVariantId == null ? item.getVariantId() == null : finalVariantId.equals(item.getVariantId())))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public void clearCart(HttpSession session) {
        Cart cart = getDbCart(session);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }
    
    private void recalculateCart(CartDTO cart) {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartItemDTO item : cart.getItems()) {
            subTotal = subTotal.add(item.getLineTotal());
        }
        cart.setSubTotal(subTotal);
        
        if (cart.getItems().isEmpty()) {
            cart.setShippingFee(BigDecimal.ZERO);
            cart.setTotal(BigDecimal.ZERO);
        } else {
            cart.setShippingFee(new BigDecimal("35000"));
            cart.setTotal(subTotal.add(cart.getShippingFee()));
        }
    }
}
