package com.ptithcm.smartshop.address.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ptithcm.smartshop.address.dto.AddressFormDTO;
import com.ptithcm.smartshop.address.dto.AddressViewDTO;

public interface AddressService {

    List<AddressViewDTO> findByUserId(UUID userId);

    Optional<AddressViewDTO> findByIdAndUserId(UUID addressId, UUID userId);

    Optional<AddressViewDTO> findDefaultByUserId(UUID userId);

    AddressViewDTO save(UUID userId, AddressFormDTO form);

    void delete(UUID userId, UUID addressId);

    AddressFormDTO toForm(AddressViewDTO address);
}