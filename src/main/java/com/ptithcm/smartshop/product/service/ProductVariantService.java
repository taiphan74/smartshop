package com.ptithcm.smartshop.product.service;

import com.ptithcm.smartshop.product.dto.ProductVariantDTO;
import com.ptithcm.smartshop.product.dto.request.ProductVariantRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductVariantService {

    List<ProductVariantDTO> findByProduct(String productId);

    Optional<ProductVariantDTO> findById(String id);

    ProductVariantDTO save(ProductVariantRequest request);

    ProductVariantDTO update(String id, ProductVariantRequest request);

    ProductVariantDTO updateStock(String id, int stockQuantity);

    ProductVariantDTO adjustStock(String id, int delta);

    ProductVariantDTO updatePrice(String id, BigDecimal price, BigDecimal compareAtPrice);

    ProductVariantDTO updateStatus(String id, boolean status);

    void deleteById(String id);
}

