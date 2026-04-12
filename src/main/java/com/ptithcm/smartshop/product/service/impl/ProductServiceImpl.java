package com.ptithcm.smartshop.product.service.impl;

import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.ProductListDTO;
import com.ptithcm.smartshop.product.dto.request.ProductRequest;
import com.ptithcm.smartshop.product.entity.Category;
import com.ptithcm.smartshop.product.entity.Product;
import com.ptithcm.smartshop.product.mapper.ProductMapper;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import com.ptithcm.smartshop.product.repository.ProductProjection;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.product.service.ProductService;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.shared.util.SlugUtil;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.shop.service.ShopService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * Triển khai nghiệp vụ quản lý sản phẩm.
 *
 * Trách nhiệm chính:
 * - Truy vấn danh sách/chi tiết sản phẩm cho UI và API.
 * - Tạo mới, cập nhật, xóa sản phẩm gắn với shop cụ thể.
 * - Kiểm tra quyền thao tác sản phẩm theo shop mà user hiện tại quản lý.
 */
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ShopService shopService;
    private final ProductMapper productMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ShopRepository shopRepository,
            ShopService shopService,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
        this.shopService = shopService;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductListDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductProjection> projections = productRepository.findAllProjection(pageable);
        return convertToPageResponse(projections);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDTO> findAllProducts() {
        List<ProductProjection> projections = productRepository.findAllProjection();
        return productMapper.toProjectionDTOList(projections);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDetailDTO> findById(String id) {
        return productRepository.findById(parseUuid(id, "id")).map(productMapper::toDetailDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDetailDTO> findBySlug(String slug) {
        return productRepository.findBySlug(slug).map(productMapper::toDetailDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductListDTO> findByCategory(String categoryId, int pageNo, int pageSize, String sortBy,
            String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ProductProjection> projections = productRepository
                .findByCategoryProjection(parseUuid(categoryId, "categoryId"), pageable);
        return convertToPageResponse(projections);
    }

    /**
     * Chuyển kết quả Page projection sang DTO page response thống nhất.
     */
    private PageResponse<ProductListDTO> convertToPageResponse(Page<ProductProjection> page) {
        List<ProductListDTO> content = productMapper.toProjectionDTOList(page.getContent());
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    @Override
    public ProductDetailDTO save(ProductRequest request) {
        // BƯỚC 1: xác thực user hiện tại có quyền quản lý shop được chọn.
        assertManagePermission(request.getShopId());

        // BƯỚC 2: sinh slug duy nhất cho sản phẩm mới.
        String baseSlug = SlugUtil.toSlug(request.getName());
        String slug = generateUniqueSlug(baseSlug);

        // BƯỚC 3: map request -> entity và gắn slug đã xử lý.
        Product product = productMapper.toEntity(request);
        product.setSlug(slug);

        // BƯỚC 4: nạp shop/category từ DB để gắn quan hệ hợp lệ.
        Shop shop = shopRepository.findById(parseUuid(request.getShopId(), "shopId"))
                .orElseThrow(() -> new ResourceNotFoundException("Shop", request.getShopId()));
        Category category = categoryRepository.findById(parseUuid(request.getCategoryId(), "categoryId"))
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        product.setShop(shop);
        product.setCategory(category);

        // BƯỚC 5: lưu sản phẩm và trả DTO chi tiết.
        Product saved = productRepository.save(product);
        return productMapper.toDetailDTO(saved);
    }

    @Override
    public ProductDetailDTO update(String id, ProductRequest request) {
        // BƯỚC 1: nạp sản phẩm hiện có để kiểm tra tồn tại và cập nhật.
        UUID productId = parseUuid(id, "id");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // BƯỚC 2: xác thực quyền trên shop đích mà request muốn gắn sản phẩm vào.
        assertManagePermission(request.getShopId());

        // BƯỚC 3: chỉ sinh lại slug nếu tên thay đổi và slug gốc cũng thay đổi.
        if (!java.util.Objects.equals(product.getName(), request.getName())) {
            String baseSlug = SlugUtil.toSlug(request.getName());
            if (!baseSlug.equals(product.getSlug())) {
                String slug = generateUniqueSlug(baseSlug);
                product.setSlug(slug);
            }
        }

        // BƯỚC 4: cập nhật dữ liệu cơ bản từ request.
        productMapper.updateEntity(request, product);

        // BƯỚC 5: nạp lại shop/category mới nhất rồi gắn vào entity.
        Shop shop = shopRepository.findById(parseUuid(request.getShopId(), "shopId"))
                .orElseThrow(() -> new ResourceNotFoundException("Shop", request.getShopId()));
        Category category = categoryRepository.findById(parseUuid(request.getCategoryId(), "categoryId"))
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        product.setShop(shop);
        product.setCategory(category);

        // BƯỚC 6: lưu thay đổi và trả DTO chi tiết.
        Product updated = productRepository.save(product);
        return productMapper.toDetailDTO(updated);
    }

    @Override
    public void deleteById(String id) {
        // BƯỚC 1: nạp sản phẩm để vừa kiểm tra tồn tại vừa xác định shop sở hữu.
        UUID productId = parseUuid(id, "id");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // BƯỚC 2: xác thực user hiện tại có quyền quản lý shop của sản phẩm.
        if (product.getShop() == null || !shopService.canManageShop(product.getShop().getId().toString())) {
            throw new AccessDeniedException("Permission denied: user cannot manage this shop");
        }

        // BƯỚC 3: xóa sản phẩm khi đã vượt qua kiểm tra quyền.
        productRepository.delete(product);
    }

    /**
     * Sinh slug duy nhất cho sản phẩm dựa trên tên đầu vào.
     */
    private String generateUniqueSlug(String baseSlug) {
        if (productRepository.findBySlug(baseSlug).isEmpty()) {
            return baseSlug;
        }

        String slug;
        do {
            slug = baseSlug + "-" + SlugUtil.randomSuffix(6);
        } while (productRepository.findBySlug(slug).isPresent());
        return slug;
    }

    /**
     * Chuyển chuỗi sang UUID và chuẩn hóa lỗi đầu vào khi parse thất bại.
     */
    private UUID parseUuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Invalid " + field + ": " + value);
        }
    }

    /**
     * Kiểm tra user hiện tại có quyền quản lý shop chỉ định hay không.
     */
    private void assertManagePermission(String shopId) {
        if (!shopService.canManageShop(shopId)) {
            throw new AccessDeniedException("Permission denied: user cannot manage this shop");
        }
    }
}
