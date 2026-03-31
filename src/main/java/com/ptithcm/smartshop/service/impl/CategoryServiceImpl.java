package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.CategoryDTO;
import com.ptithcm.smartshop.dto.PageResponse;
import com.ptithcm.smartshop.dto.request.CategoryRequest;
import com.ptithcm.smartshop.entity.Category;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.CategoryMapper;
import com.ptithcm.smartshop.repository.CategoryRepository;
import com.ptithcm.smartshop.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.ptithcm.smartshop.util.SlugUtil;

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
    public PageResponse<CategoryDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Category> categories = categoryRepository.findAll(pageable);
        return convertToPageResponse(categories);
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
    public PageResponse<CategoryDTO> findParentCategories(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Category> categories = categoryRepository.findByParentIsNull(pageable);
        return convertToPageResponse(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryDTO> findChildCategories(String parentId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", parentId));
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Category> categories = categoryRepository.findByParent(parent, pageable);
        return convertToPageResponse(categories);
    }

    private PageResponse<CategoryDTO> convertToPageResponse(Page<Category> page) {
        List<CategoryDTO> content = categoryMapper.toDTOList(page.getContent());
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public CategoryDTO save(CategoryRequest request) {
        String baseSlug = SlugUtil.toSlug(request.getName());
        String slug = generateUniqueSlug(baseSlug);
        
        Category category = categoryMapper.toEntity(request);
        category.setSlug(slug);
        
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

        // Only regenerate slug if name changed and the resulting slug is different
        if (!java.util.Objects.equals(category.getName(), request.getName())) {
            String baseSlug = SlugUtil.toSlug(request.getName());
            if (!baseSlug.equals(category.getSlug())) {
                String slug = generateUniqueSlug(baseSlug);
                category.setSlug(slug);
            }
        }

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

    private String generateUniqueSlug(String baseSlug) {
        if (!categoryRepository.findBySlug(baseSlug).isPresent()) {
            return baseSlug;
        }
        
        String slug;
        do {
            slug = baseSlug + "-" + SlugUtil.randomSuffix(6);
        } while (categoryRepository.findBySlug(slug).isPresent());
        return slug;
    }
}
