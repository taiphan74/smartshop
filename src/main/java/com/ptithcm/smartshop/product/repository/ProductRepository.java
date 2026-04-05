package com.ptithcm.smartshop.product.repository;

import com.ptithcm.smartshop.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByCategory_Id(UUID categoryId, Pageable pageable);

    Optional<Product> findBySlug(String slug);

    @Query("SELECT p.id AS id, " +
           "p.name AS name, " +
           "p.slug AS slug, " +
           "p.status AS status, " +
           "p.category.name AS categoryName, " +
           "COALESCE(" +
           "  (SELECT MIN(pv.price) FROM ProductVariant pv WHERE pv.product.id = p.id), " +
           "  0.0" +
           ") AS price, " +
           "COALESCE(" +
           "  (SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product.id = p.id AND pi.isMain = true), " +
           "  (SELECT MIN(pi2.imageUrl) FROM ProductImage pi2 WHERE pi2.product.id = p.id)" +
           ") AS thumbnailUrl " +
           "FROM Product p")
    Page<ProductProjection> findAllProjection(Pageable pageable);

    @Query("SELECT p.id AS id, " +
           "p.name AS name, " +
           "p.slug AS slug, " +
           "p.status AS status, " +
           "p.category.name AS categoryName, " +
           "COALESCE(" +
           "  (SELECT MIN(pv.price) FROM ProductVariant pv WHERE pv.product.id = p.id), " +
           "  0.0" +
           ") AS price, " +
           "COALESCE(" +
           "  (SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product.id = p.id AND pi.isMain = true), " +
           "  (SELECT MIN(pi2.imageUrl) FROM ProductImage pi2 WHERE pi2.product.id = p.id)" +
           ") AS thumbnailUrl " +
           "FROM Product p")
    List<ProductProjection> findAllProjection();

    @Query("SELECT p.id AS id, " +
           "p.name AS name, " +
           "p.slug AS slug, " +
           "p.status AS status, " +
           "p.category.name AS categoryName, " +
           "COALESCE(" +
           "  (SELECT MIN(pv.price) FROM ProductVariant pv WHERE pv.product.id = p.id), " +
           "  0.0" +
           ") AS price, " +
           "COALESCE(" +
           "  (SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product.id = p.id AND pi.isMain = true), " +
           "  (SELECT MIN(pi2.imageUrl) FROM ProductImage pi2 WHERE pi2.product.id = p.id)" +
           ") AS thumbnailUrl " +
           "FROM Product p WHERE p.category.id = :categoryId")
    Page<ProductProjection> findByCategoryProjection(UUID categoryId, Pageable pageable);
}

