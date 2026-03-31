package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.ProductDTO;
import com.ptithcm.smartshop.dto.request.ProductRequest;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    List<ProductDTO> findAll();
    
    Optional<ProductDTO> findById(String id);
    
    Optional<ProductDTO> findBySlug(String slug);
    
    List<ProductDTO> findByCategory(String categoryId);
    
    ProductDTO save(ProductRequest request);
    
    ProductDTO update(String id, ProductRequest request);
    
    void deleteById(String id);
}
