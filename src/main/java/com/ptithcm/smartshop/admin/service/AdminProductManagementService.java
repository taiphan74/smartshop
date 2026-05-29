package com.ptithcm.smartshop.admin.service;

import com.ptithcm.smartshop.admin.dto.AdminImageForm;
import com.ptithcm.smartshop.admin.dto.AdminProductForm;
import com.ptithcm.smartshop.admin.dto.AdminVariantForm;
import com.ptithcm.smartshop.product.entity.Category;
import com.ptithcm.smartshop.product.entity.Product;
import com.ptithcm.smartshop.product.entity.ProductImage;
import com.ptithcm.smartshop.product.entity.ProductVariant;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import com.ptithcm.smartshop.product.repository.ProductImageRepository;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.product.repository.ProductVariantRepository;
import com.ptithcm.smartshop.review.repository.ProductReviewRepository;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminProductManagementService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductReviewRepository reviewRepository;

    public AdminProductManagementService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ShopRepository shopRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            ProductReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> list(Boolean status, Pageable pageable) {
        if (status == null) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Product get(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
    }

    @Transactional
    public Product create(AdminProductForm form) {
        if (productRepository.existsBySlug(form.getSlug())) {
            throw new IllegalArgumentException("Slug sản phẩm đã tồn tại");
        }
        Product product = new Product();
        applyProductForm(product, form);
        return productRepository.save(product);
    }

    @Transactional
    public void update(UUID productId, AdminProductForm form) {
        Product product = get(productId);
        if (productRepository.existsBySlugAndIdNot(form.getSlug(), productId)) {
            throw new IllegalArgumentException("Slug sản phẩm đã tồn tại");
        }
        applyProductForm(product, form);
    }

    @Transactional
    public void hide(UUID productId) {
        get(productId).setStatus(false);
    }

    @Transactional
    public void show(UUID productId) {
        get(productId).setStatus(true);
    }

    @Transactional
    public void delete(UUID productId) {
        Product product = get(productId);
        if (reviewRepository.countByProductId(productId) > 0 || productRepository.countOrderItemsByProductId(productId) > 0) {
            throw new IllegalArgumentException("Sản phẩm còn đơn hàng hoặc đánh giá, hãy ẩn thay vì xóa");
        }
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> variants(UUID productId) {
        return variantRepository.findByProduct_IdOrderByCreatedAtAscIdAsc(productId);
    }

    @Transactional
    public ProductVariant createVariant(UUID productId, AdminVariantForm form) {
        if (variantRepository.existsBySkuIgnoreCase(form.getSku())) {
            throw new IllegalArgumentException("SKU đã tồn tại");
        }
        ProductVariant variant = new ProductVariant();
        variant.setProduct(get(productId));
        applyVariantForm(variant, form);
        return variantRepository.save(variant);
    }

    @Transactional
    public void updateVariant(UUID productId, UUID variantId, AdminVariantForm form) {
        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Phiên bản sản phẩm không tồn tại"));
        if (variantRepository.existsBySkuIgnoreCaseAndIdNot(form.getSku(), variantId)) {
            throw new IllegalArgumentException("SKU đã tồn tại");
        }
        applyVariantForm(variant, form);
    }

    @Transactional
    public void deleteVariant(UUID productId, UUID variantId) {
        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Phiên bản sản phẩm không tồn tại"));
        variantRepository.delete(variant);
    }

    @Transactional(readOnly = true)
    public List<ProductImage> images(UUID productId) {
        return imageRepository.findByProductIdOrderBySortOrderAsc(productId);
    }

    @Transactional
    public ProductImage createImage(UUID productId, AdminImageForm form) {
        ProductImage image = new ProductImage();
        image.setProduct(get(productId));
        applyImageForm(productId, image, form);
        return imageRepository.save(image);
    }

    @Transactional
    public void updateImage(UUID productId, UUID imageId, AdminImageForm form) {
        ProductImage image = imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Ảnh sản phẩm không tồn tại"));
        applyImageForm(productId, image, form);
    }

    @Transactional
    public void deleteImage(UUID productId, UUID imageId) {
        ProductImage image = imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Ảnh sản phẩm không tồn tại"));
        imageRepository.delete(image);
    }

    private void applyProductForm(Product product, AdminProductForm form) {
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
        Shop shop = shopRepository.findById(form.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
        product.setName(form.getName().trim());
        product.setSlug(form.getSlug().trim());
        product.setDescription(trimToNull(form.getDescription()));
        product.setStatus(form.getStatus());
        product.setCategory(category);
        product.setShop(shop);
    }

    private void applyVariantForm(ProductVariant variant, AdminVariantForm form) {
        variant.setSku(form.getSku().trim());
        variant.setPrice(form.getPrice());
        variant.setCompareAtPrice(form.getCompareAtPrice());
        variant.setStockQuantity(form.getStockQuantity());
        variant.setStatus(form.getStatus());
        variant.setThumbnailUrl(trimToNull(form.getThumbnailUrl()));
    }

    private void applyImageForm(UUID productId, ProductImage image, AdminImageForm form) {
        if (Boolean.TRUE.equals(form.getMain())) {
            imageRepository.findByProductIdOrderBySortOrderAsc(productId).forEach(existing -> existing.setIsMain(false));
        }
        image.setImageUrl(form.getImageUrl().trim());
        image.setIsMain(Boolean.TRUE.equals(form.getMain()));
        image.setSortOrder(form.getSortOrder() == null ? 0 : form.getSortOrder());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
