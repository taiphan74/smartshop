package com.ptithcm.smartshop.order.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ptithcm.smartshop.order.entity.SessionOrder;
import com.ptithcm.smartshop.order.repository.SessionOrderRepository;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/my/orders")
public class MyOrdersWebController {

    private final SessionOrderRepository sessionOrderRepository;

    public MyOrdersWebController(SessionOrderRepository sessionOrderRepository) {
        this.sessionOrderRepository = sessionOrderRepository;
    }

    @GetMapping
    public String listMyOrders(HttpSession session, Model model) {
        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser == null) {
            return "redirect:/auth/login";
        }

        List<SessionOrder> orders = findOrdersForCurrentUser(sessionUser, session.getId());
        model.addAttribute("orders", orders);
        return "order/list";
    }

    @GetMapping("/{orderCode}")
    public String viewOrderDetail(@PathVariable String orderCode,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = resolveCurrentUser(session);
        if (sessionUser == null) {
            return "redirect:/auth/login";
        }

        Optional<SessionOrder> order = resolveOrderForCurrentUser(orderCode, sessionUser, session.getId());
        if (order.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng");
            return "redirect:/my/orders";
        }

        model.addAttribute("order", order.get());
        return "order/detail";
    }

    private List<SessionOrder> findOrdersForCurrentUser(SessionUser sessionUser, String sessionId) {
        List<SessionOrder> sessionOrders = sessionOrderRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        List<SessionOrder> phoneOrders = Collections.emptyList();
        if (sessionUser.phone() != null && !sessionUser.phone().isBlank()) {
            phoneOrders = sessionOrderRepository.findByCustomerPhoneOrderByCreatedAtDesc(sessionUser.phone());
        }

        return java.util.stream.Stream.concat(sessionOrders.stream(), phoneOrders.stream())
                .collect(Collectors.toMap(SessionOrder::getOrderCode, order -> order, (left, right) -> left))
                .values()
                .stream()
                .sorted(Comparator.comparing(SessionOrder::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    private Optional<SessionOrder> resolveOrderForCurrentUser(String orderCode, SessionUser sessionUser, String sessionId) {
        Optional<SessionOrder> orderByCode = sessionOrderRepository.findByOrderCode(orderCode);
        if (orderByCode.isEmpty()) {
            return Optional.empty();
        }

        SessionOrder order = orderByCode.get();
        boolean sameSession = sessionId != null && sessionId.equals(order.getSessionId());
        boolean samePhone = sessionUser.phone() != null
                && !sessionUser.phone().isBlank()
                && sessionUser.phone().equals(order.getCustomerPhone());

        if (sameSession || samePhone) {
            return Optional.of(order);
        }
        return Optional.empty();
    }

    private SessionUser resolveCurrentUser(HttpSession session) {
        Object currentUser = session.getAttribute(SessionConstants.CURRENT_USER);
        if (currentUser instanceof SessionUser sessionUser) {
            return sessionUser;
        }
        return null;
    }
}
