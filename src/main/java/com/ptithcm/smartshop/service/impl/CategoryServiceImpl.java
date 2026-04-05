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
        Category category = categoryMapper.toEntity(request);
        String nameSlug = SlugUtil.toSlug(request.getName());
        String finalSlug;
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getParentId()));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
            
            // Logic giống Seeder: parentSlug + "-" + currentNameSlug
            finalSlug = parent.getSlug() + "-" + nameSlug;
            category.setSlug(finalSlug);
            category.setPath(parent.getPath() + "/" + finalSlug);
        } else {
            category.setLevel(0);
            finalSlug = nameSlug;
            category.setSlug(finalSlug);
            category.setPath("/" + finalSlug);
        }
        
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    @Override
    public CategoryDTO update(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        String oldPath = category.getPath();
        
        // Cập nhật Slug mới dựa trên tên và phân cấp mới
        String nameSlug = SlugUtil.toSlug(request.getName());
        categoryMapper.updateEntity(request, category);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getParentId()));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
            category.setSlug(parent.getSlug() + "-" + nameSlug);
            category.setPath(parent.getPath() + "/" + category.getSlug());
        } else {
            category.setParent(null);
            category.setLevel(0);
            category.setSlug(nameSlug);
            category.setPath("/" + category.getSlug());
        }

        Category updated = categoryRepository.save(category);
        
        if (!oldPath.equals(updated.getPath())) {
            updateChildrenPaths(updated, oldPath, updated.getPath());
        }

        return categoryMapper.toDTO(updated);
    }

    private void updateChildrenPaths(Category parent, String oldParentPath, String newParentPath) {
        List<Category> children = categoryRepository.findByParent(parent);
        for (Category child : children) {
            String oldChildPath = child.getPath();
            child.setPath(child.getPath().replaceFirst(java.util.regex.Pattern.quote(oldParentPath), newParentPath));
            child.setLevel(parent.getLevel() + 1);
            categoryRepository.save(child);
            updateChildrenPaths(child, oldChildPath, child.getPath());
        }
    }

    @Override
    public void deleteById(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }

}
