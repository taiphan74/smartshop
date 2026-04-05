package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    Page<ProductImage> findByProduct_Id(UUID productId, Pageable pageable);

    Page<ProductImage> findByProduct_IdAndIsMainTrue(UUID productId, Pageable pageable);
}
