package com.ptithcm.smartshop.voucher.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptithcm.smartshop.voucher.entity.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, String> {

    List<Voucher> findAllByOrderByCreatedAtDesc();

    Optional<Voucher> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, String id);
}