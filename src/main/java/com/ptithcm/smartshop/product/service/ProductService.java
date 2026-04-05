package com.ptithcm.smartshop.product.service;

import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.ProductListDTO;
import com.ptithcm.smartshop.product.dto.request.ProductRequest;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    PageResponse<ProductListDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir);

    List<ProductListDTO> findAllProducts();

    Optional<ProductDetailDTO> findById(String id);

    Optional<ProductDetailDTO> findBySlug(String slug);

    PageResponse<ProductListDTO> findByCategory(String categoryId, int pageNo, int pageSize, String sortBy, String sortDir);

    ProductDetailDTO save(ProductRequest request);

    ProductDetailDTO update(String id, ProductRequest request);

    void deleteById(String id);
}

