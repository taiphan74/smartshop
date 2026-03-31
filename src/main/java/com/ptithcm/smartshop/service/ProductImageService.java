package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.ProductImageDTO;
import com.ptithcm.smartshop.dto.request.ProductImageRequest;
import java.util.List;
import java.util.Optional;

public interface ProductImageService {
    
    List<ProductImageDTO> findAll();
    
    Optional<ProductImageDTO> findById(String id);
    
    List<ProductImageDTO> findByProduct(String productId);
    
    List<ProductImageDTO> findMainImage(String productId);
    
    ProductImageDTO save(ProductImageRequest request);
    
    ProductImageDTO update(String id, ProductImageRequest request);
    
    void deleteById(String id);
}
