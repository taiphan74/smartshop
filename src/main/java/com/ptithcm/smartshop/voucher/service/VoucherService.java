package com.ptithcm.smartshop.voucher.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptithcm.smartshop.cart.controller.CartSessionConstants;
import com.ptithcm.smartshop.dto.cart.CartDTO;
import com.ptithcm.smartshop.voucher.dto.VoucherListItemDTO;
import com.ptithcm.smartshop.voucher.entity.Voucher;
import com.ptithcm.smartshop.voucher.entity.VoucherDiscountType;
import com.ptithcm.smartshop.voucher.entity.VoucherScope;
import com.ptithcm.smartshop.voucher.repository.VoucherRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Transactional(readOnly = true)
    public void applyVoucher(HttpSession session, CartDTO cart, String voucherCode, VoucherScope scope) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng chưa có sản phẩm để áp voucher");
        }

        Voucher voucher = findApplicableVoucher(voucherCode, scope, cart)
                .orElseThrow(() -> new IllegalArgumentException("Mã voucher không hợp lệ hoặc chưa đủ điều kiện áp dụng"));

        setVoucherCode(session, scope, voucher.getCode());
    }

    public void removeVoucher(HttpSession session, VoucherScope scope) {
        session.removeAttribute(getSessionKey(scope));
    }

    public void clearAppliedVouchers(HttpSession session) {
        removeVoucher(session, VoucherScope.ORDER);
        removeVoucher(session, VoucherScope.SHIPPING);
    }

    @Transactional(readOnly = true)
    public void applySessionVouchers(HttpSession session, CartDTO cart) {
        cart.setOrderDiscountAmount(BigDecimal.ZERO);
        cart.setShippingDiscountAmount(BigDecimal.ZERO);
        cart.setAppliedOrderVoucherCode(null);
        cart.setAppliedOrderVoucherName(null);
        cart.setAppliedShippingVoucherCode(null);
        cart.setAppliedShippingVoucherName(null);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            clearAppliedVouchers(session);
            cart.setTotal(BigDecimal.ZERO);
            return;
        }

        applyVoucherForScope(session, cart, VoucherScope.ORDER);
        applyVoucherForScope(session, cart, VoucherScope.SHIPPING);
        BigDecimal finalTotal = cart.getTotalBeforeDiscount()
                .subtract(cart.getOrderDiscountAmount())
                .subtract(cart.getShippingDiscountAmount())
                .max(BigDecimal.ZERO);
        cart.setTotal(finalTotal);
    }

    @Transactional
    public void recordVoucherUsage(CartDTO cart) {
        incrementUsedCount(cart.getAppliedOrderVoucherCode());
        incrementUsedCount(cart.getAppliedShippingVoucherCode());
    }

    private void applyVoucherForScope(HttpSession session, CartDTO cart, VoucherScope scope) {
        String storedCode = getVoucherCode(session, scope);
        if (storedCode == null || storedCode.isBlank()) {
            return;
        }

        Optional<Voucher> voucherOpt;
        try {
            voucherOpt = findApplicableVoucher(storedCode, scope, cart);
        } catch (IllegalArgumentException exception) {
            removeVoucher(session, scope);
            return;
        }
        if (voucherOpt.isEmpty()) {
            removeVoucher(session, scope);
            return;
        }

        Voucher voucher = voucherOpt.get();
        BigDecimal discountAmount = calculateDiscount(voucher, resolveBaseAmount(voucher.getScope(), cart));
        if (scope == VoucherScope.ORDER) {
            cart.setAppliedOrderVoucherCode(voucher.getCode());
            cart.setAppliedOrderVoucherName(voucher.getName());
            cart.setOrderDiscountAmount(discountAmount);
        } else {
            cart.setAppliedShippingVoucherCode(voucher.getCode());
            cart.setAppliedShippingVoucherName(voucher.getName());
            cart.setShippingDiscountAmount(discountAmount);
        }
    }

    private Optional<Voucher> findApplicableVoucher(String voucherCode, VoucherScope scope, CartDTO cart) {
        if (voucherCode == null || voucherCode.isBlank()) {
            throw new IllegalArgumentException(scope == VoucherScope.ORDER
                    ? "Vui lòng nhập mã giảm giá đơn hàng"
                    : "Vui lòng nhập mã miễn phí vận chuyển");
        }

        Optional<Voucher> voucherOpt = voucherRepository.findByCodeIgnoreCase(voucherCode.trim());
        if (voucherOpt.isEmpty()) {
            return Optional.empty();
        }

        Voucher voucher = voucherOpt.get();
        if (!voucher.isActive() || voucher.getScope() != scope) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
            throw new IllegalArgumentException("Voucher chưa đến thời gian sử dụng");
        }
        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
            throw new IllegalArgumentException("Voucher đã hết hạn");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Voucher đã hết lượt sử dụng");
        }

        BigDecimal subTotal = cart.getSubTotal() != null ? cart.getSubTotal() : BigDecimal.ZERO;
        BigDecimal minOrderValue = voucher.getMinOrderValue() != null ? voucher.getMinOrderValue() : BigDecimal.ZERO;
        if (subTotal.compareTo(minOrderValue) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để dùng voucher này");
        }

        BigDecimal baseAmount = resolveBaseAmount(scope, cart);
        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        BigDecimal discountAmount = calculateDiscount(voucher, baseAmount);
        return discountAmount.compareTo(BigDecimal.ZERO) > 0 ? Optional.of(voucher) : Optional.empty();
    }

    private BigDecimal resolveBaseAmount(VoucherScope scope, CartDTO cart) {
        if (scope == VoucherScope.SHIPPING) {
            return cart.getShippingFee() != null ? cart.getShippingFee() : BigDecimal.ZERO;
        }
        return cart.getSubTotal() != null ? cart.getSubTotal() : BigDecimal.ZERO;
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal baseAmount) {
        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount;
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENT) {
            discountAmount = baseAmount
                    .multiply(voucher.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = voucher.getDiscountValue();
        }

        if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = discountAmount.min(voucher.getMaxDiscountAmount());
        }
        return discountAmount.min(baseAmount).max(BigDecimal.ZERO);
    }

    private void incrementUsedCount(String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return;
        }

        voucherRepository.findByCodeIgnoreCase(voucherCode.trim()).ifPresent(voucher -> {
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
        });
    }

    private void setVoucherCode(HttpSession session, VoucherScope scope, String voucherCode) {
        session.setAttribute(getSessionKey(scope), voucherCode);
    }

    private String getVoucherCode(HttpSession session, VoucherScope scope) {
        Object value = session.getAttribute(getSessionKey(scope));
        return value instanceof String text ? text : null;
    }

    private String getSessionKey(VoucherScope scope) {
        return scope == VoucherScope.ORDER
                ? CartSessionConstants.ORDER_VOUCHER_CODE
                : CartSessionConstants.SHIPPING_VOUCHER_CODE;
    }

    @Transactional(readOnly = true)
    public List<VoucherListItemDTO> getVouchersForCart(HttpSession session, CartDTO cart) {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> allVouchers = voucherRepository.findAll();

        String appliedOrderCode = getVoucherCode(session, VoucherScope.ORDER);
        String appliedShippingCode = getVoucherCode(session, VoucherScope.SHIPPING);

        return allVouchers.stream()
                .filter(v -> v.isActive()
                        && (v.getStartAt() == null || !now.isBefore(v.getStartAt()))
                        && (v.getEndAt() == null || !now.isAfter(v.getEndAt())))
                .map(v -> toListItem(v, cart, appliedOrderCode, appliedShippingCode))
                .sorted((a, b) -> {
                    // applied first, then eligible, then ineligible; within group by scope ORDER then SHIPPING
                    int scoreA = a.isApplied() ? 2 : (a.isEligible() ? 1 : 0);
                    int scoreB = b.isApplied() ? 2 : (b.isEligible() ? 1 : 0);
                    if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
                    return a.getScope().compareTo(b.getScope());
                })
                .toList();
    }

    private VoucherListItemDTO toListItem(Voucher v, CartDTO cart,
                                          String appliedOrderCode, String appliedShippingCode) {
        VoucherListItemDTO dto = new VoucherListItemDTO();
        dto.setCode(v.getCode());
        dto.setName(v.getName());
        dto.setScope(v.getScope());
        dto.setDiscountType(v.getDiscountType());
        dto.setDiscountValue(v.getDiscountValue());
        dto.setMaxDiscountAmount(v.getMaxDiscountAmount());
        dto.setMinOrderValue(v.getMinOrderValue());
        dto.setEndAt(v.getEndAt());

        boolean isApplied = v.getScope() == VoucherScope.ORDER
                ? v.getCode().equalsIgnoreCase(appliedOrderCode)
                : v.getCode().equalsIgnoreCase(appliedShippingCode);
        dto.setApplied(isApplied);

        // Check eligibility (without throwing)
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            dto.setEligible(false);
            dto.setIneligibilityReason("Giỏ hàng đang trống");
            return dto;
        }

        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
            dto.setEligible(false);
            dto.setIneligibilityReason("Voucher đã hết lượt sử dụng");
            return dto;
        }

        BigDecimal subTotal = cart.getSubTotal() != null ? cart.getSubTotal() : BigDecimal.ZERO;
        BigDecimal minOrder = v.getMinOrderValue() != null ? v.getMinOrderValue() : BigDecimal.ZERO;
        if (subTotal.compareTo(minOrder) < 0) {
            dto.setEligible(false);
            dto.setIneligibilityReason("Chưa thỏa điều kiện");
            return dto;
        }

        BigDecimal baseAmount = resolveBaseAmount(v.getScope(), cart);
        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            dto.setEligible(false);
            dto.setIneligibilityReason("Chưa thỏa điều kiện");
            return dto;
        }

        BigDecimal discount = calculateDiscount(v, baseAmount);
        dto.setEligible(true);
        dto.setCalculatedDiscount(discount);
        return dto;
    }
}