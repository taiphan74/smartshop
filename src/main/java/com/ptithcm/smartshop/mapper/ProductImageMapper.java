package com.ptithcm.smartshop.mapper;

import com.ptithcm.smartshop.dto.ProductImageDTO;
import com.ptithcm.smartshop.dto.request.ProductImageRequest;
import com.ptithcm.smartshop.entity.ProductImage;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductImageMapper {

    public ProductImageDTO toDTO(ProductImage productImage) {
        if (productImage == null) {
            return null;
        }
        
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(productImage.getId());
        dto.setProductId(productImage.getProduct() != null ? productImage.getProduct().getId() : null);
        dto.setImageUrl(productImage.getImageUrl());
        dto.setIsMain(productImage.getIsMain());
        dto.setSortOrder(productImage.getSortOrder());
        
        return dto;
    }

    public ProductImage toEntity(ProductImageRequest request) {
        if (request == null) {
            return null;
        }
        
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl(request.getImageUrl());
        productImage.setIsMain(request.getIsMain());
        productImage.setSortOrder(request.getSortOrder());
        
        return productImage;
    }

    public ProductImage updateEntity(ProductImageRequest request, ProductImage productImage) {
        if (request == null || productImage == null) {
            return productImage;
        }
        
        productImage.setImageUrl(request.getImageUrl());
        productImage.setIsMain(request.getIsMain());
        productImage.setSortOrder(request.getSortOrder());
        
        return productImage;
    }

    public List<ProductImageDTO> toDTOList(List<ProductImage> productImages) {
        if (productImages == null) {
            return new ArrayList<>();
        }
        return productImages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
