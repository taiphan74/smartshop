package com.ptithcm.smartshop.voucher.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptithcm.smartshop.voucher.entity.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, String> {

    Optional<Voucher> findByCodeIgnoreCase(String code);
}