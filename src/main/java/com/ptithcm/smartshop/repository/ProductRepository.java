package com.ptithcm.smartshop.repository;

import com.ptithcm.smartshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByCategory_Id(String categoryId);
}
