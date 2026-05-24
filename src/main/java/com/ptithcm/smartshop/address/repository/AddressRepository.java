package com.ptithcm.smartshop.address.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.ptithcm.smartshop.address.entity.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUser_IdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    Optional<Address> findByIdAndUser_Id(UUID addressId, UUID userId);

    Optional<Address> findByUser_IdAndIsDefaultTrue(UUID userId);

    @Modifying
    @Query("update Address a set a.isDefault = false where a.user.id = :userId")
    void clearDefaultByUserId(UUID userId);
}