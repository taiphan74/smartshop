package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.ProductOptionDTO;
import com.ptithcm.smartshop.dto.request.ProductOptionRequest;
import com.ptithcm.smartshop.dto.request.SortOrderUpdateRequest;

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
