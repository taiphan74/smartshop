package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {

    Page<ProductImage> findByProduct_Id(String productId, Pageable pageable);

    Page<ProductImage> findByProduct_IdAndIsMainTrue(String productId, Pageable pageable);
}
