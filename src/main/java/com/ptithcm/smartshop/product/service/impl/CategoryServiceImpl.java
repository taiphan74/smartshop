package com.ptithcm.smartshop.product.service.impl;

import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.dto.request.CategoryRequest;
import com.ptithcm.smartshop.product.entity.Category;
import com.ptithcm.smartshop.product.mapper.CategoryMapper;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import com.ptithcm.smartshop.product.service.CategoryService;
import com.ptithcm.smartshop.shared.exception.BadRequestException;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.shared.util.SlugUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        return categoryRepository.findById(parseUuid(id, "id")).map(categoryMapper::toDTO);
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
        UUID parentUuid = parseUuid(parentId, "parentId");
        Category parent = categoryRepository.findById(parentUuid)
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
        validateRequest(request);

        Category category = categoryMapper.toEntity(request);
        applyHierarchy(category, request.getName(), resolveParent(request.getParentId()));
        ensureUniqueSlug(category.getSlug(), null);

        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    @Override
    public CategoryDTO update(String id, CategoryRequest request) {
        validateRequest(request);

        UUID categoryId = parseUuid(id, "id");
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        categoryMapper.updateEntity(request, category);
        applyHierarchy(category, request.getName(), resolveParentForUpdate(category, request.getParentId()));
        ensureUniqueSlug(category.getSlug(), category.getId());

        Category updated = categoryRepository.save(category);
        syncDescendants(updated);
        return categoryMapper.toDTO(updated);
    }

    private void syncDescendants(Category parent) {
        List<Category> children = categoryRepository.findByParent(parent);
        for (Category child : children) {
            applyHierarchy(child, child.getName(), parent);
            ensureUniqueSlug(child.getSlug(), child.getId());
            categoryRepository.save(child);
            syncDescendants(child);
        }
    }

    @Override
    public void deleteById(String id) {
        UUID categoryId = parseUuid(id, "id");
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(categoryId);
    }

    private UUID parseUuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Invalid " + field + ": " + value);
        }
    }

    private void validateRequest(CategoryRequest request) {
        if (request == null) {
            throw new BadRequestException("request", "must not be null");
        }
    }

    private Category resolveParent(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }

        UUID parentUuid = parseUuid(parentId, "parentId");
        return categoryRepository.findById(parentUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Category", parentId));
    }

    private Category resolveParentForUpdate(Category category, String parentId) {
        Category parent = resolveParent(parentId);
        if (parent == null) {
            return null;
        }

        if (parent.getId().equals(category.getId())) {
            throw new BadRequestException("parentId", "cannot be the same as the category id");
        }

        Category current = parent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new BadRequestException("parentId", "cannot be a descendant of the category");
            }
            current = current.getParent();
        }

        return parent;
    }

    private void applyHierarchy(Category category, String name, Category parent) {
        String ownNameSlug = SlugUtil.toSlug(name);
        if (ownNameSlug.isBlank()) {
            throw new BadRequestException("name", "must contain at least one valid character");
        }

        category.setParent(parent);
        if (parent == null) {
            category.setLevel(0);
            category.setSlug(ownNameSlug);
            category.setPath("/" + ownNameSlug);
            return;
        }

        String slug = parent.getSlug() + "-" + ownNameSlug;
        category.setLevel(parent.getLevel() + 1);
        category.setSlug(slug);
        category.setPath(parent.getPath() + "/" + slug);
    }

    private void ensureUniqueSlug(String slug, UUID categoryId) {
        boolean exists = categoryId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, categoryId);

        if (exists) {
            throw new ConflictException("Category", "slug");
        }
    }
}
