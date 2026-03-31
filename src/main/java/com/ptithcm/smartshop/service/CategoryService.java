package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.CategoryDTO;
import com.ptithcm.smartshop.dto.request.CategoryRequest;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    
    List<CategoryDTO> findAll();
    
    Optional<CategoryDTO> findById(String id);
    
    Optional<CategoryDTO> findBySlug(String slug);
    
    List<CategoryDTO> findParentCategories();
    
    List<CategoryDTO> findChildCategories(String parentId);
    
    CategoryDTO save(CategoryRequest request);
    
    CategoryDTO update(String id, CategoryRequest request);
    
    void deleteById(String id);
}
