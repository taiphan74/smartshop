package com.ptithcm.smartshop.banner.service;

import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BannerService {

    List<Banner> findAllActive();

    List<Banner> findAll();

    Banner findById(UUID id);

    Banner create(BannerForm form, MultipartFile imageFile);

    Banner update(UUID id, BannerForm form, MultipartFile imageFile);

    void delete(UUID id);
}
