package com.ptithcm.smartshop.product.repository;

import com.ptithcm.smartshop.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsByPath(String path);

    boolean existsByPathAndIdNot(String path, UUID id);

    long countByParentId(UUID parentId);

    Page<Category> findByParentIsNull(Pageable pageable);

    Page<Category> findByParent(Category parent, Pageable pageable);

    List<Category> findByParent(Category parent);
}

