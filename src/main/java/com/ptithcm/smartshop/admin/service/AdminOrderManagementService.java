package com.ptithcm.smartshop.admin.service;

import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import com.ptithcm.smartshop.order.domain.repository.OrderRepository;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrderManagementService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.SHIPPING, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    private final OrderRepository orderRepository;

    public AdminOrderManagementService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<Order> list(OrderStatus status, Pageable pageable) {
        if (status == null) {
            return orderRepository.findByOrderByCreatedAtDesc(pageable);
        }
        return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Transactional(readOnly = true)
    public Order get(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
    }

    @Transactional
    public void changeStatus(UUID orderId, OrderStatus nextStatus) {
        Order order = get(orderId);
        if (!ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), Set.of()).contains(nextStatus)) {
            throw new IllegalArgumentException("Không thể chuyển trạng thái đơn hàng");
        }
        order.setStatus(nextStatus);
    }

    public Set<OrderStatus> allowedNextStatuses(OrderStatus status) {
        return ALLOWED_TRANSITIONS.getOrDefault(status, Set.of());
    }
}
