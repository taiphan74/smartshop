package com.ptithcm.smartshop.seller.service.impl;

import com.ptithcm.smartshop.seller.service.SellerDashboardService;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SellerDashboardServiceImpl implements SellerDashboardService {
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public SellerDashboardServiceImpl(ProductRepository productRepository, 
                                       OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Map<String, Object> getDashboardMetrics(UUID shopId) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalProducts = productRepository.countByShopId(shopId);
        long totalOrders = orderRepository.countByShopId(shopId);
        
        BigDecimal totalRevenue = orderRepository.sumFinalAmountByShopId(shopId);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        metrics.put("totalRevenue", totalRevenue.longValue());
        metrics.put("totalOrders", totalOrders);
        metrics.put("totalProducts", totalProducts);
        return metrics;
    }
}
