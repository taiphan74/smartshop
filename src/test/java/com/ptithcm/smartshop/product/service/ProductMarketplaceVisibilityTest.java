package com.ptithcm.smartshop.product.service;

import com.ptithcm.smartshop.product.mapper.ProductMapper;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import com.ptithcm.smartshop.product.repository.ProductProjection;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductMarketplaceVisibilityTest {

    @Test
    void findAllProductsUsesApprovedShopQuery() {
        ProductRepository productRepository = mock(ProductRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        ProductServiceImpl service = new ProductServiceImpl(productRepository, categoryRepository, productMapper);
        ProductProjection projection = mock(ProductProjection.class);
        when(productRepository.findPublicProductsFromApprovedShops()).thenReturn(List.of(projection));
        when(productMapper.toProjectionDTOList(List.of(projection))).thenReturn(List.of());

        service.findAllProducts();

        verify(productRepository).findPublicProductsFromApprovedShops();
    }
}
