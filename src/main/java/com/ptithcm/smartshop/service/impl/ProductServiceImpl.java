package com.ptithcm.smartshop.service.impl;

import com.ptithcm.smartshop.dto.PageResponse;
import com.ptithcm.smartshop.dto.ProductDetailDTO;
import com.ptithcm.smartshop.dto.ProductListDTO;
import com.ptithcm.smartshop.dto.request.ProductRequest;
import com.ptithcm.smartshop.entity.Category;
import com.ptithcm.smartshop.entity.Product;
import com.ptithcm.smartshop.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.mapper.ProductMapper;
import com.ptithcm.smartshop.repository.CategoryRepository;
import com.ptithcm.smartshop.repository.ProductProjection;
import com.ptithcm.smartshop.repository.ProductRepository;
import com.ptithcm.smartshop.service.ProductService;
import com.ptithcm.smartshop.util.SlugUtil;
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
    public PageResponse<ProductListDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductProjection> projections = productRepository.findAllProjection(pageable);
        return convertToPageResponse(projections);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDetailDTO> findById(String id) {
        return productRepository.findById(id).map(productMapper::toDetailDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDetailDTO> findBySlug(String slug) {
        return productRepository.findBySlug(slug).map(productMapper::toDetailDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductListDTO> findByCategory(String categoryId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductProjection> projections = productRepository.findByCategoryProjection(categoryId, pageable);
        return convertToPageResponse(projections);
    }

    private PageResponse<ProductListDTO> convertToPageResponse(Page<ProductProjection> page) {
        List<ProductListDTO> content = productMapper.toProjectionDTOList(page.getContent());
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
    public ProductDetailDTO save(ProductRequest request) {
        String baseSlug = SlugUtil.toSlug(request.getName());
        String slug = generateUniqueSlug(baseSlug);

        Product product = productMapper.toEntity(request);
        product.setSlug(slug);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return productMapper.toDetailDTO(saved);
    }

    @Override
    public ProductDetailDTO update(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // Only regenerate slug if name changed and the resulting slug is different
        if (!java.util.Objects.equals(product.getName(), request.getName())) {
            String baseSlug = SlugUtil.toSlug(request.getName());
            if (!baseSlug.equals(product.getSlug())) {
                String slug = generateUniqueSlug(baseSlug);
                product.setSlug(slug);
            }
        }

        productMapper.updateEntity(request, product);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        product.setCategory(category);

        Product updated = productRepository.save(product);
        return productMapper.toDetailDTO(updated);
    }

    @Override
    public void deleteById(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }

    private String generateUniqueSlug(String baseSlug) {
        if (!productRepository.findBySlug(baseSlug).isPresent()) {
            return baseSlug;
        }
        
        String slug;
        do {
            slug = baseSlug + "-" + SlugUtil.randomSuffix(6);
        } while (productRepository.findBySlug(slug).isPresent());
        return slug;
    }
}
