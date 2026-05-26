package com.ptithcm.smartshop.admin.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptithcm.smartshop.admin.dto.AdminVoucherForm;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.voucher.entity.Voucher;
import com.ptithcm.smartshop.voucher.repository.VoucherRepository;

@Service
public class AdminVoucherManagementService {

    private final VoucherRepository voucherRepository;

    public AdminVoucherManagementService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Transactional(readOnly = true)
    public List<Voucher> findAll() {
        return voucherRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public AdminVoucherForm buildCreateForm() {
        AdminVoucherForm form = new AdminVoucherForm();
        form.setMinOrderValue(BigDecimal.ZERO);
        form.setActive(true);
        return form;
    }

    @Transactional(readOnly = true)
    public AdminVoucherForm buildEditForm(String id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));

        AdminVoucherForm form = new AdminVoucherForm();
        form.setCode(voucher.getCode());
        form.setName(voucher.getName());
        form.setScope(voucher.getScope());
        form.setDiscountType(voucher.getDiscountType());
        form.setDiscountValue(voucher.getDiscountValue());
        form.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        form.setMinOrderValue(voucher.getMinOrderValue());
        form.setUsageLimit(voucher.getUsageLimit());
        form.setActive(voucher.isActive());
        form.setStartAt(voucher.getStartAt());
        form.setEndAt(voucher.getEndAt());
        return form;
    }

    @Transactional
    public void create(AdminVoucherForm form) {
        String normalizedCode = normalizeCode(form.getCode());
        if (voucherRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new IllegalArgumentException("Ma voucher da ton tai");
        }

        validateBusinessRules(form);

        Voucher voucher = new Voucher();
        applyForm(voucher, form, normalizedCode);
        voucherRepository.save(voucher);
    }

    @Transactional
    public void update(String id, AdminVoucherForm form) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));

        String normalizedCode = normalizeCode(form.getCode());
        if (voucherRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new IllegalArgumentException("Ma voucher da ton tai");
        }

        validateBusinessRules(form);
        applyForm(voucher, form, normalizedCode);
        voucherRepository.save(voucher);
    }

    @Transactional
    public void toggleActive(String id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));
        voucher.setActive(!voucher.isActive());
        voucherRepository.save(voucher);
    }

    @Transactional
    public void delete(String id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));
        voucherRepository.delete(voucher);
    }

    private void applyForm(Voucher voucher, AdminVoucherForm form, String normalizedCode) {
        voucher.setCode(normalizedCode);
        voucher.setName(trimOrNull(form.getName()));
        voucher.setScope(form.getScope());
        voucher.setDiscountType(form.getDiscountType());
        voucher.setDiscountValue(form.getDiscountValue());
        voucher.setMaxDiscountAmount(form.getMaxDiscountAmount());
        voucher.setMinOrderValue(form.getMinOrderValue());
        voucher.setUsageLimit(form.getUsageLimit());
        voucher.setActive(form.isActive());
        voucher.setStartAt(form.getStartAt());
        voucher.setEndAt(form.getEndAt());
    }

    private void validateBusinessRules(AdminVoucherForm form) {
        LocalDateTime startAt = form.getStartAt();
        LocalDateTime endAt = form.getEndAt();
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        if (startAt != null && startAt.isBefore(now)) {
            throw new IllegalArgumentException("Thoi gian bat dau phai lon hon hoac bang thoi gian hien tai");
        }

        if (endAt != null && !endAt.isAfter(now)) {
            throw new IllegalArgumentException("Thoi gian ket thuc phai lon hon thoi gian hien tai");
        }

        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("Thoi gian ket thuc phai sau thoi gian bat dau");
        }

        if (form.getDiscountType() != null
                && form.getDiscountType().name().equals("PERCENT")
                && form.getDiscountValue() != null
                && form.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Voucher phan tram khong duoc vuot qua 100%");
        }
    }

    private String normalizeCode(String code) {
        return trimOrNull(code).toUpperCase();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
