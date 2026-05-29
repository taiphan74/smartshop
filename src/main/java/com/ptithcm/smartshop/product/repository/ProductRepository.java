package com.ptithcm.smartshop.product.repository;

import com.ptithcm.smartshop.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    interface CategoryProductCountProjection {
        String getCategoryName();

        long getProductCount();
    }

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.shop JOIN FETCH p.category",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAll(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.shop JOIN FETCH p.category WHERE p.status = :status",
           countQuery = "SELECT count(p) FROM Product p WHERE p.status = :status")
    Page<Product> findByStatus(Boolean status, Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.shop JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findById(@Param("id") UUID id);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.shop JOIN FETCH p.category WHERE p.slug = :slug")
    Optional<Product> findBySlug(@Param("slug") String slug);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.shop JOIN FETCH p.category WHERE p.category.id = :categoryId",
           countQuery = "SELECT count(p) FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategory_Id(UUID categoryId, Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

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
           ") AS thumbnailUrl, " +
           "p.reviewCount AS reviewCount, " +
           "p.averageRating AS averageRating " +
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
           ") AS thumbnailUrl, " +
           "p.reviewCount AS reviewCount, " +
           "p.averageRating AS averageRating " +
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
           ") AS thumbnailUrl, " +
           "p.reviewCount AS reviewCount, " +
           "p.averageRating AS averageRating " +
           "FROM Product p JOIN p.shop s " +
           "WHERE p.status = true AND s.status = 'APPROVED'")
    List<ProductProjection> findPublicProductsFromApprovedShops();

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
           ") AS thumbnailUrl, " +
           "p.reviewCount AS reviewCount, " +
           "p.averageRating AS averageRating " +
           "FROM Product p JOIN p.shop s JOIN p.category c " +
           "WHERE p.status = true AND s.status = 'APPROVED' " +
           "AND (c.path = :categoryPath OR c.path LIKE CONCAT(:categoryPath, '/%'))")
    List<ProductProjection> findPublicProductsByCategoryPath(String categoryPath);

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
           ") AS thumbnailUrl, " +
           "p.reviewCount AS reviewCount, " +
           "p.averageRating AS averageRating " +
           "FROM Product p WHERE p.category.id = :categoryId")
    Page<ProductProjection> findByCategoryProjection(UUID categoryId, Pageable pageable);

    @Modifying
    @Query("""
            update Product p
            set p.reviewCount = p.reviewCount + 1,
                p.ratingSum = p.ratingSum + :rating,
                p.averageRating = ((p.ratingSum + :rating) * 1.0 / (p.reviewCount + 1))
            where p.id = :productId
            """)
    void incrementRatingSummary(UUID productId, int rating);

    long countByStatusTrue();

    long countByShopId(UUID shopId);

    long countByCategoryId(UUID categoryId);

    @Query("select count(i.id) from OrderItem i where i.product.id = :productId")
    long countOrderItemsByProductId(@Param("productId") UUID productId);

    @Query("""
            select p.category.name as categoryName, count(p.id) as productCount
            from Product p
            where p.status = true
            group by p.category.name
            order by count(p.id) desc, p.category.name asc
            """)
    List<CategoryProductCountProjection> countActiveProductsByCategory();


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
           ") AS thumbnailUrl, " +
           "p.reviewCount AS reviewCount, " +
           "p.averageRating AS averageRating " +
           "FROM Product p JOIN p.shop s " +
           "WHERE p.status = true AND s.status = 'APPROVED' " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ProductProjection> searchPublicProducts(@Param("keyword") String keyword, Pageable pageable);
}
