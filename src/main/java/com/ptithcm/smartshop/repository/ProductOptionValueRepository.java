package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, String> {

    List<ProductOptionValue> findByOption_IdOrderBySortOrderAscIdAsc(String optionId);

    boolean existsByOption_IdAndValueIgnoreCase(String optionId, String value);

    boolean existsByOption_IdAndValueIgnoreCaseAndIdNot(String optionId, String value, String id);

    @Query("select count(v) from ProductVariant v join v.optionValues ov where ov.option.id = :optionId")
    long countVariantUsageByOptionId(@Param("optionId") String optionId);

    @Query("select count(v) from ProductVariant v join v.optionValues ov where ov.id = :optionValueId")
    long countVariantUsageByOptionValueId(@Param("optionValueId") String optionValueId);
}
