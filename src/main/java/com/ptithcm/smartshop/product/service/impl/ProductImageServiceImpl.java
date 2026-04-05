package com.ptithcm.smartshop.product.service.impl;

import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.dto.ProductImageDTO;
import com.ptithcm.smartshop.product.dto.request.ProductImageRequest;
import com.ptithcm.smartshop.product.entity.Product;
import com.ptithcm.smartshop.product.entity.ProductImage;
import com.ptithcm.smartshop.shared.exception.BadRequestException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.product.mapper.ProductImageMapper;
import com.ptithcm.smartshop.product.repository.ProductImageRepository;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.product.service.ProductImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductImageMapper productImageMapper;

    public ProductImageServiceImpl(ProductImageRepository productImageRepository,
                                   ProductRepository productRepository,
                                   ProductImageMapper productImageMapper) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.productImageMapper = productImageMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductImageDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductImage> productImages = productImageRepository.findAll(pageable);
        return convertToPageResponse(productImages);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductImageDTO> findById(String id) {
        return productImageRepository.findById(parseUuid(id, "id")).map(productImageMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductImageDTO> findByProduct(String productId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductImage> productImages = productImageRepository.findByProduct_Id(parseUuid(productId, "productId"), pageable);
        return convertToPageResponse(productImages);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductImageDTO> findMainImage(String productId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductImage> productImages = productImageRepository.findByProduct_IdAndIsMainTrue(parseUuid(productId, "productId"), pageable);
        return convertToPageResponse(productImages);
    }

    private PageResponse<ProductImageDTO> convertToPageResponse(Page<ProductImage> page) {
        List<ProductImageDTO> content = productImageMapper.toDTOList(page.getContent());
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public ProductImageDTO save(ProductImageRequest request) {
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            throw new BadRequestException("imageUrl", "is required");
        }
        
        ProductImage productImage = productImageMapper.toEntity(request);
        
        Product product = productRepository.findById(parseUuid(request.getProductId(), "productId"))
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        productImage.setProduct(product);
        
        ProductImage saved = productImageRepository.save(productImage);
        return productImageMapper.toDTO(saved);
    }

    @Override
    public ProductImageDTO update(String id, ProductImageRequest request) {
        ProductImage productImage = productImageRepository.findById(parseUuid(id, "id"))
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", id));
        
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            throw new BadRequestException("imageUrl", "is required");
        }
        
        productImageMapper.updateEntity(request, productImage);
        
        Product product = productRepository.findById(parseUuid(request.getProductId(), "productId"))
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        productImage.setProduct(product);
        
        ProductImage updated = productImageRepository.save(productImage);
        return productImageMapper.toDTO(updated);
    }

    @Override
    public void deleteById(String id) {
        UUID imageId = parseUuid(id, "id");
        if (!productImageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("ProductImage", id);
        }
        productImageRepository.deleteById(imageId);
    }

    private UUID parseUuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new BadRequestException(field, "must be a valid UUID");
        }
    }
}


