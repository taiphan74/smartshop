package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.PageResponse;
import com.ptithcm.smartshop.dto.ProductImageDTO;
import com.ptithcm.smartshop.dto.request.ProductImageRequest;
import java.util.Optional;

public interface ProductImageService {
    
    PageResponse<ProductImageDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir);
    
    Optional<ProductImageDTO> findById(String id);
    
    PageResponse<ProductImageDTO> findByProduct(String productId, int pageNo, int pageSize, String sortBy, String sortDir);
    
    PageResponse<ProductImageDTO> findMainImage(String productId, int pageNo, int pageSize, String sortBy, String sortDir);
    
    ProductImageDTO save(ProductImageRequest request);
    
    ProductImageDTO update(String id, ProductImageRequest request);
    
    void deleteById(String id);
}
