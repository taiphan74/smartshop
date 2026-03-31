package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.ProductDTO;
import com.ptithcm.smartshop.dto.request.ProductRequest;
import com.ptithcm.smartshop.entity.Category;
import com.ptithcm.smartshop.entity.Product;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.ProductMapper;
import com.ptithcm.smartshop.repository.CategoryRepository;
import com.ptithcm.smartshop.repository.ProductRepository;
import com.ptithcm.smartshop.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.ptithcm.smartshop.util.SlugUtil;
import com.ptithcm.smartshop.exception.ConflictException;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, 
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        return productMapper.toDTOList(productRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findById(String id) {
        return productRepository.findById(id).map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findBySlug(String slug) {
        return productRepository.findBySlug(slug).map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findByCategory(String categoryId) {
        return productMapper.toDTOList(productRepository.findByCategory_Id(categoryId));
    }

    @Override
    public ProductDTO save(ProductRequest request) {
        String slug = StringUtils.hasText(request.getSlug()) 
                ? SlugUtil.toSlug(request.getSlug()) 
                : SlugUtil.toSlug(request.getName());

        if (productRepository.findBySlug(slug).isPresent()) {
            throw new ConflictException("Product", "slug");
        }

        Product product = productMapper.toEntity(request);
        product.setSlug(slug);
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        product.setCategory(category);
        
        Product saved = productRepository.save(product);
        return productMapper.toDTO(saved);
    }

    @Override
    public ProductDTO update(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        
        String slug = StringUtils.hasText(request.getSlug()) 
                ? SlugUtil.toSlug(request.getSlug()) 
                : SlugUtil.toSlug(request.getName());

        Optional<Product> existingProduct = productRepository.findBySlug(slug);
        if (existingProduct.isPresent() && !existingProduct.get().getId().equals(id)) {
            throw new ConflictException("Product", "slug");
        }

        productMapper.updateEntity(request, product);
        product.setSlug(slug);
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        product.setCategory(category);
        
        Product updated = productRepository.save(product);
        return productMapper.toDTO(updated);
    }

    @Override
    public void deleteById(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }
}
