package com.ptithcm.smartshop.product.mapper;

import com.ptithcm.smartshop.product.dto.*;
import com.ptithcm.smartshop.product.dto.request.ProductRequest;
import com.ptithcm.smartshop.product.entity.Product;
import com.ptithcm.smartshop.product.entity.ProductImage;
import com.ptithcm.smartshop.product.entity.ProductOption;
import com.ptithcm.smartshop.product.entity.ProductOptionValue;
import com.ptithcm.smartshop.product.entity.ProductVariant;
import com.ptithcm.smartshop.product.repository.ProductProjection;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductListDTO toListDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductListDTO dto = new ProductListDTO();
        dto.setId(Objects.toString(product.getId(), null));
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setPrice(product.getVariants().stream()
                .map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));
        dto.setStatus(product.getStatus());

        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getShop() != null) {
            dto.setShopId(Objects.toString(product.getShop().getId(), null));
            dto.setShopSlug(product.getShop().getSlug());
            dto.setShopName(product.getShop().getName());
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
        dto.setId(Objects.toString(product.getId(), null));
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getVariants().stream()
                .map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));
        dto.setStockQuantity(product.getVariants().stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            dto.setCategoryId(Objects.toString(product.getCategory().getId(), null));
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getShop() != null) {
            dto.setShopId(Objects.toString(product.getShop().getId(), null));
            dto.setShopSlug(product.getShop().getSlug());
            dto.setShopName(product.getShop().getName());
        }

        if (product.getImages() != null) {
            dto.setImages(product.getImages().stream()
                    .map(this::imageToDTO)
                    .collect(Collectors.toList()));
        }

        if (product.getOptions() != null) {
            dto.setOptions(product.getOptions().stream()
                    .map(this::toOptionDTO)
                    .collect(Collectors.toList()));
        }

        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(this::toVariantDTO)
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
        product.setStatus(request.getStatus());
        
        return product;
    }

    public Product updateEntity(ProductRequest request, Product product) {
        if (request == null || product == null) {
            return product;
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());
        
        return product;
    }

    public ProductImageDTO imageToDTO(ProductImage image) {
        if (image == null) {
            return null;
        }
        
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(Objects.toString(image.getId(), null));
        dto.setProductId(image.getProduct() != null ? Objects.toString(image.getProduct().getId(), null) : null);
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
        dto.setShopId(projection.getShopId());
        dto.setShopSlug(projection.getShopSlug());
        dto.setShopName(projection.getShopName());
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

    public ProductVariantDTO toVariantDTO(ProductVariant variant) {
        if (variant == null) return null;
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(Objects.toString(variant.getId(), null));
        dto.setSku(variant.getSku());
        dto.setPrice(variant.getPrice());
        dto.setCompareAtPrice(variant.getCompareAtPrice());
        dto.setStockQuantity(variant.getStockQuantity());
        dto.setThumbnailUrl(variant.getThumbnailUrl());
        if (variant.getOptionValues() != null) {
            dto.setOptionValues(variant.getOptionValues().stream()
                    .map(this::toOptionValueDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public ProductOptionDTO toOptionDTO(ProductOption option) {
        if (option == null) return null;
        ProductOptionDTO dto = new ProductOptionDTO();
        dto.setId(Objects.toString(option.getId(), null));
        dto.setName(option.getName());
        dto.setSortOrder(option.getSortOrder());
        if (option.getValues() != null) {
            dto.setValues(option.getValues().stream()
                    .map(this::toOptionValueDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public ProductOptionValueDTO toOptionValueDTO(ProductOptionValue value) {
        if (value == null) return null;
        ProductOptionValueDTO dto = new ProductOptionValueDTO();
        dto.setId(Objects.toString(value.getId(), null));
        dto.setValue(value.getValue());
        dto.setSortOrder(value.getSortOrder());
        return dto;
    }
}

