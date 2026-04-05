package com.ptithcm.smartshop.product.repository;

import com.ptithcm.smartshop.product.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, UUID> {

    List<ProductOptionValue> findByOption_IdOrderBySortOrderAscIdAsc(UUID optionId);

    boolean existsByOption_IdAndValueIgnoreCase(UUID optionId, String value);

    boolean existsByOption_IdAndValueIgnoreCaseAndIdNot(UUID optionId, String value, UUID id);

    @Query("select count(v) from ProductVariant v join v.optionValues ov where ov.option.id = :optionId")
    long countVariantUsageByOptionId(@Param("optionId") UUID optionId);

    @Query("select count(v) from ProductVariant v join v.optionValues ov where ov.id = :optionValueId")
    long countVariantUsageByOptionValueId(@Param("optionValueId") UUID optionValueId);
}

