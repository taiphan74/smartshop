package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, String> {

    List<ProductOption> findByProduct_IdOrderBySortOrderAscIdAsc(String productId);

    boolean existsByProduct_IdAndNameIgnoreCase(String productId, String name);

    boolean existsByProduct_IdAndNameIgnoreCaseAndIdNot(String productId, String name, String id);
}
