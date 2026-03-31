package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.ProductOptionDTO;
import com.ptithcm.smartshop.dto.request.ProductOptionRequest;
import com.ptithcm.smartshop.dto.request.SortOrderUpdateRequest;
import com.ptithcm.smartshop.entity.Product;
import com.ptithcm.smartshop.entity.ProductOption;
import com.ptithcm.smartshop.exception.BadRequestException;
import com.ptithcm.smartshop.exception.ConflictException;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.ProductMapper;
import com.ptithcm.smartshop.repository.ProductOptionRepository;
import com.ptithcm.smartshop.repository.ProductOptionValueRepository;
import com.ptithcm.smartshop.repository.ProductRepository;
import com.ptithcm.smartshop.service.ProductOptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ProductOptionServiceImpl implements ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductOptionServiceImpl(ProductOptionRepository productOptionRepository,
                                    ProductOptionValueRepository productOptionValueRepository,
                                    ProductRepository productRepository,
                                    ProductMapper productMapper) {
        this.productOptionRepository = productOptionRepository;
        this.productOptionValueRepository = productOptionValueRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionDTO> findByProduct(String productId) {
        validateRequired(productId, "productId");
        return productOptionRepository.findByProduct_IdOrderBySortOrderAscIdAsc(productId)
                .stream()
                .map(productMapper::toOptionDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductOptionDTO> findById(String id) {
        return productOptionRepository.findById(id).map(productMapper::toOptionDTO);
    }

    @Override
    public ProductOptionDTO save(ProductOptionRequest request) {
        validateOptionRequest(request);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        String normalizedName = normalize(request.getName());
        if (productOptionRepository.existsByProduct_IdAndNameIgnoreCase(product.getId(), normalizedName)) {
            throw new ConflictException("ProductOption", "name");
        }

        ProductOption option = new ProductOption();
        option.setProduct(product);
        option.setName(normalizedName);
        option.setSortOrder(defaultSortOrder(request.getSortOrder()));

        return productMapper.toOptionDTO(productOptionRepository.save(option));
    }

    @Override
    public ProductOptionDTO update(String id, ProductOptionRequest request) {
        ProductOption option = productOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOption", id));

        validateRequired(request.getName(), "name");
        String normalizedName = normalize(request.getName());

        if (productOptionRepository.existsByProduct_IdAndNameIgnoreCaseAndIdNot(
                option.getProduct().getId(), normalizedName, option.getId())) {
            throw new ConflictException("ProductOption", "name");
        }

        option.setName(normalizedName);
        if (request.getSortOrder() != null) {
            option.setSortOrder(defaultSortOrder(request.getSortOrder()));
        }

        return productMapper.toOptionDTO(productOptionRepository.save(option));
    }

    @Override
    public List<ProductOptionDTO> updateSortOrders(String productId, List<SortOrderUpdateRequest> requests) {
        validateRequired(productId, "productId");
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("sortOrders", "must not be empty");
        }

        List<ProductOption> options = productOptionRepository.findByProduct_IdOrderBySortOrderAscIdAsc(productId);
        for (SortOrderUpdateRequest request : requests) {
            ProductOption option = options.stream()
                    .filter(item -> Objects.equals(item.getId(), request.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("sortOrders", "contains option outside product"));
            option.setSortOrder(defaultSortOrder(request.getSortOrder()));
        }

        return productOptionRepository.saveAll(options).stream()
                .map(productMapper::toOptionDTO)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        ProductOption option = productOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOption", id));

        if (productOptionValueRepository.countVariantUsageByOptionId(option.getId()) > 0) {
            throw new ConflictException("ProductOption", "is already used by variants");
        }

        productOptionRepository.delete(option);
    }

    private void validateOptionRequest(ProductOptionRequest request) {
        if (request == null) {
            throw new BadRequestException("request", "must not be null");
        }
        validateRequired(request.getProductId(), "productId");
        validateRequired(request.getName(), "name");
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
}
