package com.ptithcm.smartshop.product.service;

import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.ProductListDTO;
import com.ptithcm.smartshop.product.dto.request.ProductRequest;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    /**
     * Lấy danh sách sản phẩm phân trang cho các luồng quản trị/tra cứu.
     */
    PageResponse<ProductListDTO> findAll(int pageNo, int pageSize, String sortBy, String sortDir);

    List<ProductListDTO> findAllProducts();

    Optional<ProductDetailDTO> findById(String id);

    Optional<ProductDetailDTO> findBySlug(String slug);

    PageResponse<ProductListDTO> findByCategory(String categoryId, int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Tạo mới sản phẩm cho một shop mà user hiện tại có quyền quản lý.
     */
    ProductDetailDTO save(ProductRequest request);

    /**
     * Cập nhật sản phẩm hiện có và xác thực quyền trên shop được gắn.
     */
    ProductDetailDTO update(String id, ProductRequest request);

    /**
     * Xóa sản phẩm khi user hiện tại có quyền thao tác trên shop của sản phẩm.
     */
    void deleteById(String id);
}

