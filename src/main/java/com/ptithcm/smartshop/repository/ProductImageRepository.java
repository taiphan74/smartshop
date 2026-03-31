package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {

    List<ProductImage> findByProduct_Id(String productId);

    List<ProductImage> findByProduct_IdAndIsMainTrue(String productId);
}
