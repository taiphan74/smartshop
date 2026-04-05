package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.ProductVariantDTO;
import com.ptithcm.smartshop.dto.request.ProductVariantRequest;
import com.ptithcm.smartshop.entity.Product;
import com.ptithcm.smartshop.entity.ProductOptionValue;
import com.ptithcm.smartshop.entity.ProductVariant;
import com.ptithcm.smartshop.exception.BadRequestException;
import com.ptithcm.smartshop.exception.ConflictException;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.ProductMapper;
import com.ptithcm.smartshop.repository.ProductOptionValueRepository;
import com.ptithcm.smartshop.repository.ProductRepository;
import com.ptithcm.smartshop.repository.ProductVariantRepository;
import com.ptithcm.smartshop.service.ProductVariantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductVariantServiceImpl(ProductVariantRepository productVariantRepository,
                                     ProductOptionValueRepository productOptionValueRepository,
                                     ProductRepository productRepository,
                                     ProductMapper productMapper) {
        this.productVariantRepository = productVariantRepository;
        this.productOptionValueRepository = productOptionValueRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDTO> findByProduct(String productId) {
        validateRequired(productId, "productId");
        return productVariantRepository.findByProduct_IdOrderByCreatedAtAscIdAsc(productId)
                .stream()
                .map(productMapper::toVariantDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductVariantDTO> findById(String id) {
        return productVariantRepository.findWithOptionValuesById(id).map(productMapper::toVariantDTO);
    }

    @Override
    public ProductVariantDTO save(ProductVariantRequest request) {
        validateVariantRequest(request);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (productVariantRepository.existsBySkuIgnoreCase(normalize(request.getSku()))) {
            throw new ConflictException("ProductVariant", "sku");
        }

        List<ProductOptionValue> optionValues = resolveAndValidateOptionValues(product.getId(), request.getOptionValueIds());
        ensureUniqueVariantCombination(product.getId(), optionValues, null);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        applyVariantData(variant, request, optionValues);

        return productMapper.toVariantDTO(productVariantRepository.save(variant));
    }

    @Override
    public ProductVariantDTO update(String id, ProductVariantRequest request) {
        validateVariantRequest(request);

        ProductVariant variant = productVariantRepository.findWithOptionValuesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", id));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (!Objects.equals(variant.getProduct().getId(), product.getId())) {
            throw new BadRequestException("productId", "cannot change variant product");
        }

        String normalizedSku = normalize(request.getSku());
        if (productVariantRepository.existsBySkuIgnoreCaseAndIdNot(normalizedSku, variant.getId())) {
            throw new ConflictException("ProductVariant", "sku");
        }

        List<ProductOptionValue> optionValues = resolveAndValidateOptionValues(product.getId(), request.getOptionValueIds());
        ensureUniqueVariantCombination(product.getId(), optionValues, variant.getId());

        applyVariantData(variant, request, optionValues);

        return productMapper.toVariantDTO(productVariantRepository.save(variant));
    }

    @Override
    public ProductVariantDTO updateStock(String id, int stockQuantity) {
        ProductVariant variant = getVariantOrThrow(id);
        validateStock(stockQuantity);
        variant.setStockQuantity(stockQuantity);
        return productMapper.toVariantDTO(productVariantRepository.save(variant));
    }

    @Override
    public ProductVariantDTO adjustStock(String id, int delta) {
        ProductVariant variant = getVariantOrThrow(id);
        int nextStock = variant.getStockQuantity() + delta;
        validateStock(nextStock);
        variant.setStockQuantity(nextStock);
        return productMapper.toVariantDTO(productVariantRepository.save(variant));
    }

    @Override
    public ProductVariantDTO updatePrice(String id, BigDecimal price, BigDecimal compareAtPrice) {
        ProductVariant variant = getVariantOrThrow(id);
        validatePrice(price);
        validateCompareAtPrice(price, compareAtPrice);
        variant.setPrice(price);
        variant.setCompareAtPrice(compareAtPrice);
        return productMapper.toVariantDTO(productVariantRepository.save(variant));
    }

    @Override
    public ProductVariantDTO updateStatus(String id, boolean status) {
        ProductVariant variant = getVariantOrThrow(id);
        variant.setStatus(status);
        return productMapper.toVariantDTO(productVariantRepository.save(variant));
    }

    @Override
    public void deleteById(String id) {
        ProductVariant variant = getVariantOrThrow(id);
        productVariantRepository.delete(variant);
    }

    private ProductVariant getVariantOrThrow(String id) {
        return productVariantRepository.findWithOptionValuesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", id));
    }

    private void applyVariantData(ProductVariant variant, ProductVariantRequest request, List<ProductOptionValue> optionValues) {
        String normalizedSku = normalize(request.getSku());
        validatePrice(request.getPrice());
        validateCompareAtPrice(request.getPrice(), request.getCompareAtPrice());
        validateStock(request.getStockQuantity());

        variant.setSku(normalizedSku);
        variant.setPrice(request.getPrice());
        variant.setCompareAtPrice(request.getCompareAtPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setStatus(request.getStatus() != null ? request.getStatus() : Boolean.TRUE);
        variant.setThumbnailUrl(normalizeNullable(request.getThumbnailUrl()));
        variant.setOptionValues(optionValues);
    }

    private List<ProductOptionValue> resolveAndValidateOptionValues(String productId, List<String> optionValueIds) {
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            throw new BadRequestException("optionValueIds", "must not be empty");
        }

        Set<String> requestedIds = new HashSet<>();
        for (String optionValueId : optionValueIds) {
            validateRequired(optionValueId, "optionValueId");
            if (!requestedIds.add(optionValueId)) {
                throw new BadRequestException("optionValueIds", "contains duplicates");
            }
        }

        List<ProductOptionValue> optionValues = productOptionValueRepository.findAllById(optionValueIds);
        if (optionValues.size() != optionValueIds.size()) {
            throw new BadRequestException("optionValueIds", "contains invalid ids");
        }

        Set<String> optionIds = new HashSet<>();
        for (ProductOptionValue optionValue : optionValues) {
            String ownerProductId = optionValue.getOption().getProduct().getId();
            if (!Objects.equals(ownerProductId, productId)) {
                throw new BadRequestException("optionValueIds", "must belong to the same product");
            }
            if (!optionIds.add(optionValue.getOption().getId())) {
                throw new BadRequestException("optionValueIds", "cannot contain multiple values of the same option");
            }
        }

        return optionValues;
    }

    private void ensureUniqueVariantCombination(String productId, List<ProductOptionValue> optionValues, String currentVariantId) {
        Set<String> requestedValueIds = optionValues.stream()
                .map(ProductOptionValue::getId)
                .collect(java.util.stream.Collectors.toSet());

        boolean duplicate = productVariantRepository.findByProduct_IdOrderByCreatedAtAscIdAsc(productId).stream()
                .filter(variant -> !Objects.equals(variant.getId(), currentVariantId))
                .anyMatch(variant -> variant.getOptionValues().size() == requestedValueIds.size()
                        && variant.getOptionValues().stream().map(ProductOptionValue::getId).collect(java.util.stream.Collectors.toSet()).equals(requestedValueIds));

        if (duplicate) {
            throw new ConflictException("ProductVariant", "optionValues");
        }
    }

    private void validateVariantRequest(ProductVariantRequest request) {
        if (request == null) {
            throw new BadRequestException("request", "must not be null");
        }
        validateRequired(request.getProductId(), "productId");
        validateRequired(request.getSku(), "sku");
        if (request.getPrice() == null) {
            throw new BadRequestException("price", "is required");
        }
        if (request.getStockQuantity() == null) {
            throw new BadRequestException("stockQuantity", "is required");
        }
        validatePrice(request.getPrice());
        validateCompareAtPrice(request.getPrice(), request.getCompareAtPrice());
        validateStock(request.getStockQuantity());
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new BadRequestException("price", "is required");
        }
        if (price.signum() < 0) {
            throw new BadRequestException("price", "must be greater than or equal to 0");
        }
    }

    private void validateCompareAtPrice(BigDecimal price, BigDecimal compareAtPrice) {
        if (compareAtPrice != null && compareAtPrice.signum() < 0) {
            throw new BadRequestException("compareAtPrice", "must be greater than or equal to 0");
        }
        if (compareAtPrice != null && price != null && compareAtPrice.compareTo(price) < 0) {
            throw new BadRequestException("compareAtPrice", "must be greater than or equal to price");
        }
    }

    private void validateStock(Integer stockQuantity) {
        if (stockQuantity == null) {
            throw new BadRequestException("stockQuantity", "is required");
        }
        if (stockQuantity < 0) {
            throw new BadRequestException("stockQuantity", "must be greater than or equal to 0");
        }
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(field, "is required");
        }
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
