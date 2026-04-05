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

    Page<Category> findByParentIsNull(Pageable pageable);

    Page<Category> findByParent(Category parent, Pageable pageable);

    List<Category> findByParent(Category parent);
}

