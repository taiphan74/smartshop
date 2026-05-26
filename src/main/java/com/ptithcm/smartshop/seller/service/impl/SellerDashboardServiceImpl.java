package com.ptithcm.smartshop.seller.service.impl;

import com.ptithcm.smartshop.seller.service.SellerDashboardService;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.order.domain.repository.OrderRepository;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SellerDashboardServiceImpl implements SellerDashboardService {
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;

    public SellerDashboardServiceImpl(ProductRepository productRepository, 
                                       OrderRepository orderRepository,
                                       ShopRepository shopRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    public Map<String, Object> getDashboardMetrics(UUID userId) {
        Map<String, Object> metrics = new HashMap<>();
        
        List<Shop> shops = shopRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
        if (shops.isEmpty()) {
            metrics.put("totalRevenue", 0);
            metrics.put("totalOrders", 0);
            metrics.put("totalProducts", 0);
            return metrics;
        }
        
        UUID shopId = shops.get(0).getId();
        
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
