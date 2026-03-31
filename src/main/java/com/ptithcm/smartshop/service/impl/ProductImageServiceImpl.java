package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.PageResponse;
import com.ptithcm.smartshop.dto.ProductImageDTO;
import com.ptithcm.smartshop.dto.request.ProductImageRequest;
import com.ptithcm.smartshop.entity.Product;
import com.ptithcm.smartshop.entity.ProductImage;
import com.ptithcm.smartshop.exception.BadRequestException;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.ProductImageMapper;
import com.ptithcm.smartshop.repository.ProductImageRepository;
import com.ptithcm.smartshop.repository.ProductRepository;
import com.ptithcm.smartshop.service.ProductImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

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
        return productImageRepository.findById(id).map(productImageMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductImageDTO> findByProduct(String productId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductImage> productImages = productImageRepository.findByProduct_Id(productId, pageable);
        return convertToPageResponse(productImages);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductImageDTO> findMainImage(String productId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductImage> productImages = productImageRepository.findByProduct_IdAndIsMainTrue(productId, pageable);
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
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        productImage.setProduct(product);
        
        ProductImage saved = productImageRepository.save(productImage);
        return productImageMapper.toDTO(saved);
    }

    @Override
    public ProductImageDTO update(String id, ProductImageRequest request) {
        ProductImage productImage = productImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", id));
        
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            throw new BadRequestException("imageUrl", "is required");
        }
        
        productImageMapper.updateEntity(request, productImage);
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        productImage.setProduct(product);
        
        ProductImage updated = productImageRepository.save(productImage);
        return productImageMapper.toDTO(updated);
    }

    @Override
    public void deleteById(String id) {
        if (!productImageRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProductImage", id);
        }
        productImageRepository.deleteById(id);
    }
}
