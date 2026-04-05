package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {

    @EntityGraph(attributePaths = "optionValues")
    List<ProductVariant> findByProduct_IdOrderByCreatedAtAscIdAsc(String productId);

    @EntityGraph(attributePaths = "optionValues")
    Optional<ProductVariant> findWithOptionValuesById(String id);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, String id);
}
