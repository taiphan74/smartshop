package com.ptithcm.smartshop.banner.service.impl;

import com.ptithcm.smartshop.banner.config.BannerUploadConfig;
import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import com.ptithcm.smartshop.banner.repository.BannerRepository;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.shared.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class BannerServiceImpl implements BannerService {

    private static final String URL_PREFIX = "/uploads/banners";

    private final BannerRepository bannerRepository;
    private final BannerUploadConfig uploadConfig;

    @Value("${banner.upload.dir:src/main/resources/static/uploads/banners}")
    private String uploadDir;

    public BannerServiceImpl(BannerRepository bannerRepository, BannerUploadConfig uploadConfig) {
        this.bannerRepository = bannerRepository;
        this.uploadConfig = uploadConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> findAllActive() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> findAll() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Banner findById(UUID id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", id.toString()));
    }

    @Override
    @Transactional
    public Banner create(BannerForm form, MultipartFile imageFile) {
        Banner banner = new Banner();
        banner.setTitle(form.title());
        banner.setLinkUrl(form.linkUrl());
        banner.setDisplayOrder(form.displayOrder());
        banner.setIsActive(form.isActive());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = FileUploadUtil.saveFile(imageFile, uploadConfig.getUploadPath(), URL_PREFIX);
            banner.setImageUrl(imageUrl);
        } else {
            banner.setImageUrl(form.imageUrl());
        }

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public Banner update(UUID id, BannerForm form, MultipartFile imageFile) {
        Banner banner = findById(id);

        banner.setTitle(form.title());
        banner.setLinkUrl(form.linkUrl());
        banner.setDisplayOrder(form.displayOrder());
        banner.setIsActive(form.isActive());

        if (imageFile != null && !imageFile.isEmpty()) {
            FileUploadUtil.deleteFile(banner.getImageUrl(), uploadConfig.getUploadPath(), URL_PREFIX);
            String imageUrl = FileUploadUtil.saveFile(imageFile, uploadConfig.getUploadPath(), URL_PREFIX);
            banner.setImageUrl(imageUrl);
        } else if (form.imageUrl() != null && !form.imageUrl().isBlank()) {
            banner.setImageUrl(form.imageUrl());
        }

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Banner banner = findById(id);
        FileUploadUtil.deleteFile(banner.getImageUrl(), uploadConfig.getUploadPath(), URL_PREFIX);
        bannerRepository.delete(banner);
    }
}
