package com.ptithcm.smartshop.product.service;

import com.ptithcm.smartshop.product.dto.ProductOptionValueDTO;
import com.ptithcm.smartshop.product.dto.request.ProductOptionValueRequest;
import com.ptithcm.smartshop.product.dto.request.SortOrderUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface ProductOptionValueService {

    List<ProductOptionValueDTO> findByOption(String optionId);

    Optional<ProductOptionValueDTO> findById(String id);

    ProductOptionValueDTO save(ProductOptionValueRequest request);

    ProductOptionValueDTO update(String id, ProductOptionValueRequest request);

    List<ProductOptionValueDTO> updateSortOrders(String optionId, List<SortOrderUpdateRequest> requests);

    void deleteById(String id);
}

