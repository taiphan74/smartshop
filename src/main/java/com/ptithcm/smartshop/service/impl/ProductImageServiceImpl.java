package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.ProductImageDTO;
import com.ptithcm.smartshop.dto.request.ProductImageRequest;
import com.ptithcm.smartshop.entity.Product;
import com.ptithcm.smartshop.entity.ProductImage;
import com.ptithcm.smartshop.exception.BadRequestException;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.ProductImageMapper;
import com.ptithcm.smartshop.repository.ProductRepository;
import com.ptithcm.smartshop.repository.ProductImageRepository;
import com.ptithcm.smartshop.service.ProductImageService;
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
    public List<ProductImageDTO> findAll() {
        return productImageMapper.toDTOList(productImageRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductImageDTO> findById(String id) {
        return productImageRepository.findById(id).map(productImageMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDTO> findByProduct(String productId) {
        return productImageMapper.toDTOList(productImageRepository.findByProduct_Id(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDTO> findMainImage(String productId) {
        return productImageMapper.toDTOList(productImageRepository.findByProduct_IdAndIsMainTrue(productId));
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
