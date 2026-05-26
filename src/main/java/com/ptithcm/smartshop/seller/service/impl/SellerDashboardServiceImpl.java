package com.ptithcm.smartshop.seller.service.impl;

import com.ptithcm.smartshop.seller.service.SellerDashboardService;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SellerDashboardServiceImpl implements SellerDashboardService {
    
    private final ProductRepository productRepository;

    public SellerDashboardServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Map<String, Object> getDashboardMetrics(UUID shopId) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Đếm số lượng sản phẩm thực tế từ hệ thống
        long totalProducts = productRepository.count(); 
        
        // Dữ liệu mock ban đầu cho các chỉ số tài chính và đơn hàng
        metrics.put("totalRevenue", 15450000); 
        metrics.put("totalOrders", 24);
        metrics.put("totalProducts", totalProducts);
        return metrics;
    }
}
