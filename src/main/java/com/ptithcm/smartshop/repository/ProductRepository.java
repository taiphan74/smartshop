package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Page<Product> findByCategory_Id(String categoryId, Pageable pageable);
    
    Optional<Product> findBySlug(String slug);
}
