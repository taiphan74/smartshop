package com.ptithcm.smartshop.mapper;

import com.ptithcm.smartshop.dto.ProductDetailDTO;
import com.ptithcm.smartshop.dto.ProductImageDTO;
import com.ptithcm.smartshop.dto.ProductListDTO;
import com.ptithcm.smartshop.dto.request.ProductRequest;
import com.ptithcm.smartshop.entity.Product;
import com.ptithcm.smartshop.entity.ProductImage;
import com.ptithcm.smartshop.repository.ProductProjection;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductListDTO toListDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductListDTO dto = new ProductListDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setPrice(product.getPrice());
        dto.setStatus(product.getStatus());

        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            dto.setThumbnailUrl(product.getImages().stream()
                    .filter(ProductImage::getIsMain)
                    .findFirst()
                    .map(ProductImage::getImageUrl)
                    .orElse(product.getImages().get(0).getImageUrl()));
        }

        return dto;
    }

    public ProductDetailDTO toDetailDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getImages() != null) {
            dto.setImages(product.getImages().stream()
                    .map(this::imageToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(request.getStatus());
        
        return product;
    }

    public Product updateEntity(ProductRequest request, Product product) {
        if (request == null || product == null) {
            return product;
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(request.getStatus());
        
        return product;
    }

    public ProductImageDTO imageToDTO(ProductImage image) {
        if (image == null) {
            return null;
        }
        
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(image.getId());
        dto.setProductId(image.getProduct() != null ? image.getProduct().getId() : null);
        dto.setImageUrl(image.getImageUrl());
        dto.setIsMain(image.getIsMain());
        dto.setSortOrder(image.getSortOrder());
        
        return dto;
    }

    public List<ProductListDTO> toListDTOList(List<Product> products) {
        if (products == null) {
            return new ArrayList<>();
        }
        return products.stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDetailDTO> toDetailDTOList(List<Product> products) {
        if (products == null) {
            return new ArrayList<>();
        }
        return products.stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    public ProductListDTO toListDTO(ProductProjection projection) {
        if (projection == null) {
            return null;
        }

        ProductListDTO dto = new ProductListDTO();
        dto.setId(projection.getId());
        dto.setName(projection.getName());
        dto.setSlug(projection.getSlug());
        dto.setPrice(projection.getPrice());
        dto.setStatus(projection.getStatus());
        dto.setCategoryName(projection.getCategoryName());
        dto.setThumbnailUrl(projection.getThumbnailUrl());
        return dto;
    }

    public List<ProductListDTO> toProjectionDTOList(List<ProductProjection> projections) {
        if (projections == null) {
            return new ArrayList<>();
        }
        return projections.stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
    }
}
