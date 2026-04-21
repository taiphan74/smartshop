package com.ptithcm.smartshop.address.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptithcm.smartshop.address.dto.AddressFormDTO;
import com.ptithcm.smartshop.address.dto.AddressViewDTO;
import com.ptithcm.smartshop.address.entity.Address;
import com.ptithcm.smartshop.address.repository.AddressRepository;
import com.ptithcm.smartshop.address.service.AddressService;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressViewDTO> findByUserId(UUID userId) {
        return addressRepository.findByUser_IdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AddressViewDTO> findByIdAndUserId(UUID addressId, UUID userId) {
        return addressRepository.findByIdAndUser_Id(addressId, userId).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AddressViewDTO> findDefaultByUserId(UUID userId) {
        return addressRepository.findByUser_IdAndIsDefaultTrue(userId).map(this::toView);
    }

    @Override
    @Transactional
    public AddressViewDTO save(UUID userId, AddressFormDTO form) {
        Address address = resolveAddressToSave(userId, form);

        address.setReceiverName(form.getReceiverName().trim());
        address.setPhone(form.getPhone().trim());
        address.setProvince(form.getProvince().trim());
        address.setDistrict(form.getDistrict().trim());
        address.setWard(form.getWard().trim());
        address.setDetail(form.getDetail().trim());

        boolean hasDefault = addressRepository.findByUser_IdAndIsDefaultTrue(userId).isPresent();
        boolean shouldBeDefault = form.isDefault() || !hasDefault;
        if (shouldBeDefault) {
            addressRepository.clearDefaultByUserId(userId);
        }
        address.setDefault(shouldBeDefault);

        return toView(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID addressId) {
        Optional<Address> addressOpt = addressRepository.findByIdAndUser_Id(addressId, userId);
        if (addressOpt.isEmpty()) {
            return;
        }

        Address address = addressOpt.get();
        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            Optional<Address> next = addressRepository.findByUser_IdOrderByIsDefaultDescCreatedAtDesc(userId)
                    .stream()
                    .findFirst();
            if (next.isPresent()) {
                Address promote = next.get();
                promote.setDefault(true);
                addressRepository.save(promote);
            }
        }
    }

    @Override
    public AddressFormDTO toForm(AddressViewDTO address) {
        AddressFormDTO form = new AddressFormDTO();
        form.setId(address.getId());
        form.setReceiverName(address.getReceiverName());
        form.setPhone(address.getPhone());
        form.setProvince(address.getProvince());
        form.setDistrict(address.getDistrict());
        form.setWard(address.getWard());
        form.setDetail(address.getDetail());
        form.setDefault(address.isDefault());
        return form;
    }

    private Address resolveAddressToSave(UUID userId, AddressFormDTO form) {
        if (form.getId() != null) {
            return addressRepository.findByIdAndUser_Id(form.getId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Address address = new Address();
        address.setUser(user);
        return address;
    }

    private AddressViewDTO toView(Address address) {
        AddressViewDTO view = new AddressViewDTO();
        view.setId(address.getId());
        view.setReceiverName(address.getReceiverName());
        view.setPhone(address.getPhone());
        view.setProvince(address.getProvince());
        view.setDistrict(address.getDistrict());
        view.setWard(address.getWard());
        view.setDetail(address.getDetail());
        view.setDefault(address.isDefault());
        return view;
    }
}