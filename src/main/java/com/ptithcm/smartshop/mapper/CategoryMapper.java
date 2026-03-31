package com.ptithcm.smartshop.mapper;

import com.ptithcm.smartshop.dto.CategoryDTO;
import com.ptithcm.smartshop.dto.request.CategoryRequest;
import com.ptithcm.smartshop.entity.Category;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }
        
        if (category.getChildren() != null) {
            dto.setChildren(category.getChildren().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    public Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        Category category = new Category();
        category.setName(request.getName());
        
        return category;
    }

    public Category updateEntity(CategoryRequest request, Category category) {
        if (request == null || category == null) {
            return category;
        }
        
        category.setName(request.getName());
        
        return category;
    }

    public List<CategoryDTO> toDTOList(List<Category> categories) {
        if (categories == null) {
            return new ArrayList<>();
        }
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
