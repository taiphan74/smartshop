package com.ptithcm.smartshop.product.service;

import com.ptithcm.smartshop.product.dto.ProductOptionDTO;
import com.ptithcm.smartshop.product.dto.request.ProductOptionRequest;
import com.ptithcm.smartshop.product.dto.request.SortOrderUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface ProductOptionService {

    List<ProductOptionDTO> findByProduct(String productId);

    Optional<ProductOptionDTO> findById(String id);

    ProductOptionDTO save(ProductOptionRequest request);

    ProductOptionDTO update(String id, ProductOptionRequest request);

    List<ProductOptionDTO> updateSortOrders(String productId, List<SortOrderUpdateRequest> requests);

    void deleteById(String id);
}

