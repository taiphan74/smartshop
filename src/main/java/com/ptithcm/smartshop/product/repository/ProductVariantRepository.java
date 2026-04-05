package com.ptithcm.smartshop.product.repository;

import com.ptithcm.smartshop.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    @EntityGraph(attributePaths = "optionValues")
    List<ProductVariant> findByProduct_IdOrderByCreatedAtAscIdAsc(UUID productId);

    @EntityGraph(attributePaths = "optionValues")
    Optional<ProductVariant> findWithOptionValuesById(UUID id);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, UUID id);
}

