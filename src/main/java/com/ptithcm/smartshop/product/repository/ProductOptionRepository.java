package com.ptithcm.smartshop.product.repository;

import com.ptithcm.smartshop.product.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, UUID> {

    List<ProductOption> findByProduct_IdOrderBySortOrderAscIdAsc(UUID productId);

    boolean existsByProduct_IdAndNameIgnoreCase(UUID productId, String name);

    boolean existsByProduct_IdAndNameIgnoreCaseAndIdNot(UUID productId, String name, UUID id);
}

