package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.CategoryDTO;
import com.ptithcm.smartshop.dto.PageResponse;
import com.ptithcm.smartshop.dto.request.CategoryRequest;
import java.util.Optional;

public interface CategoryService {
    
    PageResponse<CategoryDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir);
    
    Optional<CategoryDTO> findById(String id);
    
    Optional<CategoryDTO> findBySlug(String slug);
    
    PageResponse<CategoryDTO> findParentCategories(int pageNo, int pageSize, String sortBy, String sortDir);
    
    PageResponse<CategoryDTO> findChildCategories(String parentId, int pageNo, int pageSize, String sortBy, String sortDir);
    
    CategoryDTO save(CategoryRequest request);
    
    CategoryDTO update(String id, CategoryRequest request);
    
    void deleteById(String id);
}
