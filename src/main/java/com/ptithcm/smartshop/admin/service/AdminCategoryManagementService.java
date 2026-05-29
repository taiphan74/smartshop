package com.ptithcm.smartshop.admin.service;

import com.ptithcm.smartshop.admin.dto.AdminCategoryForm;
import com.ptithcm.smartshop.product.entity.Category;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCategoryManagementService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryManagementService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Category> list(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Category get(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
    }

    @Transactional
    public Category create(AdminCategoryForm form) {
        if (categoryRepository.existsBySlug(form.getSlug())) {
            throw new IllegalArgumentException("Slug danh mục đã tồn tại");
        }
        if (categoryRepository.existsByPath(form.getPath())) {
            throw new IllegalArgumentException("Path danh mục đã tồn tại");
        }
        Category category = new Category();
        applyForm(category, form);
        return categoryRepository.save(category);
    }

    @Transactional
    public void update(UUID categoryId, AdminCategoryForm form) {
        Category category = get(categoryId);
        if (categoryRepository.existsBySlugAndIdNot(form.getSlug(), categoryId)) {
            throw new IllegalArgumentException("Slug danh mục đã tồn tại");
        }
        if (categoryRepository.existsByPathAndIdNot(form.getPath(), categoryId)) {
            throw new IllegalArgumentException("Path danh mục đã tồn tại");
        }
        applyForm(category, form);
    }

    @Transactional
    public void delete(UUID categoryId) {
        Category category = get(categoryId);
        if (productRepository.countByCategoryId(categoryId) > 0 || categoryRepository.countByParentId(categoryId) > 0) {
            throw new IllegalArgumentException("Danh mục còn sản phẩm hoặc danh mục con");
        }
        categoryRepository.delete(category);
    }

    private void applyForm(Category category, AdminCategoryForm form) {
        category.setName(form.getName().trim());
        category.setSlug(form.getSlug().trim());
        category.setPath(form.getPath().trim());
        category.setLevel(form.getLevel());
        category.setParent(form.getParentId() == null ? null : get(form.getParentId()));
    }
}
