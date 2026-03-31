package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.CategoryDTO;
import com.ptithcm.smartshop.dto.request.CategoryRequest;
import com.ptithcm.smartshop.entity.Category;
import com.ptithcm.smartshop.exception.ConflictException;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.CategoryMapper;
import com.ptithcm.smartshop.repository.CategoryRepository;
import com.ptithcm.smartshop.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        return categoryMapper.toDTOList(categoryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> findById(String id) {
        return categoryRepository.findById(id).map(categoryMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug).map(categoryMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> findParentCategories() {
        return categoryMapper.toDTOList(categoryRepository.findByParentIsNull());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> findChildCategories(String parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", parentId));
        return categoryMapper.toDTOList(categoryRepository.findByParent(parent));
    }

    @Override
    public CategoryDTO save(CategoryRequest request) {
        if (request.getSlug() != null && categoryRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new ConflictException("Category", "slug");
        }
        
        Category category = categoryMapper.toEntity(request);
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getParentId()));
            category.setParent(parent);
        }
        
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    @Override
    public CategoryDTO update(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        
        categoryMapper.updateEntity(request, category);
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        Category updated = categoryRepository.save(category);
        return categoryMapper.toDTO(updated);
    }

    @Override
    public void deleteById(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }
}
