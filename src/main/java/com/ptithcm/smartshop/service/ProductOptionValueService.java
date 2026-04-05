package com.ptithcm.smartshop.service;

import com.ptithcm.smartshop.dto.ProductOptionValueDTO;
import com.ptithcm.smartshop.dto.request.ProductOptionValueRequest;
import com.ptithcm.smartshop.dto.request.SortOrderUpdateRequest;

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
