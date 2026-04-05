package com.ptithcm.smartshop.product.service.impl;

import com.ptithcm.smartshop.product.dto.ProductOptionValueDTO;
import com.ptithcm.smartshop.product.dto.request.ProductOptionValueRequest;
import com.ptithcm.smartshop.product.dto.request.SortOrderUpdateRequest;
import com.ptithcm.smartshop.product.entity.ProductOption;
import com.ptithcm.smartshop.product.entity.ProductOptionValue;
import com.ptithcm.smartshop.shared.exception.BadRequestException;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.product.mapper.ProductMapper;
import com.ptithcm.smartshop.product.repository.ProductOptionRepository;
import com.ptithcm.smartshop.product.repository.ProductOptionValueRepository;
import com.ptithcm.smartshop.product.service.ProductOptionValueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductOptionValueServiceImpl implements ProductOptionValueService {

    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductMapper productMapper;

    public ProductOptionValueServiceImpl(ProductOptionValueRepository productOptionValueRepository,
                                         ProductOptionRepository productOptionRepository,
                                         ProductMapper productMapper) {
        this.productOptionValueRepository = productOptionValueRepository;
        this.productOptionRepository = productOptionRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionValueDTO> findByOption(String optionId) {
        validateRequired(optionId, "optionId");
        return productOptionValueRepository.findByOption_IdOrderBySortOrderAscIdAsc(parseUuid(optionId, "optionId"))
                .stream()
                .map(productMapper::toOptionValueDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductOptionValueDTO> findById(String id) {
        return productOptionValueRepository.findById(parseUuid(id, "id")).map(productMapper::toOptionValueDTO);
    }

    @Override
    public ProductOptionValueDTO save(ProductOptionValueRequest request) {
        validateRequest(request);

        ProductOption option = productOptionRepository.findById(parseUuid(request.getOptionId(), "optionId"))
                .orElseThrow(() -> new ResourceNotFoundException("ProductOption", request.getOptionId()));

        String normalizedValue = normalize(request.getValue());
        if (productOptionValueRepository.existsByOption_IdAndValueIgnoreCase(option.getId(), normalizedValue)) {
            throw new ConflictException("ProductOptionValue", "value");
        }

        ProductOptionValue value = new ProductOptionValue();
        value.setOption(option);
        value.setValue(normalizedValue);
        value.setSortOrder(defaultSortOrder(request.getSortOrder()));

        return productMapper.toOptionValueDTO(productOptionValueRepository.save(value));
    }

    @Override
    public ProductOptionValueDTO update(String id, ProductOptionValueRequest request) {
        ProductOptionValue value = productOptionValueRepository.findById(parseUuid(id, "id"))
                .orElseThrow(() -> new ResourceNotFoundException("ProductOptionValue", id));

        validateRequired(request.getValue(), "value");
        String normalizedValue = normalize(request.getValue());

        if (productOptionValueRepository.existsByOption_IdAndValueIgnoreCaseAndIdNot(
                value.getOption().getId(), normalizedValue, value.getId())) {
            throw new ConflictException("ProductOptionValue", "value");
        }

        value.setValue(normalizedValue);
        if (request.getSortOrder() != null) {
            value.setSortOrder(defaultSortOrder(request.getSortOrder()));
        }

        return productMapper.toOptionValueDTO(productOptionValueRepository.save(value));
    }

    @Override
    public List<ProductOptionValueDTO> updateSortOrders(String optionId, List<SortOrderUpdateRequest> requests) {
        validateRequired(optionId, "optionId");
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("sortOrders", "must not be empty");
        }

        List<ProductOptionValue> values = productOptionValueRepository.findByOption_IdOrderBySortOrderAscIdAsc(parseUuid(optionId, "optionId"));
        for (SortOrderUpdateRequest request : requests) {
            UUID requestId = parseUuid(request.getId(), "id");
            ProductOptionValue value = values.stream()
                    .filter(item -> Objects.equals(item.getId(), requestId))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("sortOrders", "contains value outside option"));
            value.setSortOrder(defaultSortOrder(request.getSortOrder()));
        }

        return productOptionValueRepository.saveAll(values).stream()
                .map(productMapper::toOptionValueDTO)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        ProductOptionValue value = productOptionValueRepository.findById(parseUuid(id, "id"))
                .orElseThrow(() -> new ResourceNotFoundException("ProductOptionValue", id));

        if (productOptionValueRepository.countVariantUsageByOptionValueId(value.getId()) > 0) {
            throw new ConflictException("ProductOptionValue", "is already used by variants");
        }

        productOptionValueRepository.delete(value);
    }

    private void validateRequest(ProductOptionValueRequest request) {
        if (request == null) {
            throw new BadRequestException("request", "must not be null");
        }
        validateRequired(request.getOptionId(), "optionId");
        validateRequired(request.getValue(), "value");
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(field, "is required");
        }
    }

    private String normalize(String value) {
        return value.trim();
    }

    private int defaultSortOrder(Integer sortOrder) {
        return sortOrder != null ? sortOrder : 0;
    }

    private UUID parseUuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new BadRequestException(field, "must be a valid UUID");
        }
    }
}


