package com.ptithcm.smartshop.product.mapper;

import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.request.CategoryRequest;
import com.ptithcm.smartshop.product.entity.Category;
import com.ptithcm.smartshop.product.entity.CategoryTranslation;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(Objects.toString(category.getId(), null));
        dto.setName(category.getName());
        dto.setDisplayName(resolveDisplayName(category));
        dto.setSlug(category.getSlug());
        dto.setPath(category.getPath());
        dto.setLevel(category.getLevel());
        
        if (category.getParent() != null) {
            dto.setParentId(Objects.toString(category.getParent().getId(), null));
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

    private String resolveDisplayName(Category category) {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return category.getTranslations().stream()
                .filter(translation -> language.equals(translation.getLocale()))
                .map(CategoryTranslation::getName)
                .findFirst()
                .orElse(category.getName());
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

