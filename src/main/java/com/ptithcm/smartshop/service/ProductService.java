package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.PageResponse;
import com.ptithcm.smartshop.dto.ProductDTO;
import com.ptithcm.smartshop.dto.request.ProductRequest;
import java.util.Optional;

public interface ProductService {
    
    PageResponse<ProductDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir);
    
    Optional<ProductDTO> findById(String id);
    
    Optional<ProductDTO> findBySlug(String slug);
    
    PageResponse<ProductDTO> findByCategory(String categoryId, int pageNo, int pageSize, String sortBy, String sortDir);
    
    ProductDTO save(ProductRequest request);
    
    ProductDTO update(String id, ProductRequest request);
    
    void deleteById(String id);
}
