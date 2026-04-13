package com.ptithcm.smartshop.banner.service.impl;

import com.ptithcm.smartshop.banner.config.BannerUploadConfig;
import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import com.ptithcm.smartshop.banner.repository.BannerRepository;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BannerUploadConfig uploadConfig;

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
            String imageUrl = saveImage(imageFile);
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
            deleteImage(banner.getImageUrl());
            String imageUrl = saveImage(imageFile);
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
        deleteImage(banner.getImageUrl());
        bannerRepository.delete(banner);
    }

    private String saveImage(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        try {
            Files.copy(file.getInputStream(), uploadConfig.getUploadPath().resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + fileName, e);
        }

        return "/uploads/banners/" + fileName;
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/banners/")) {
            return;
        }
        String fileName = imageUrl.substring("/uploads/banners/".length());
        try {
            Files.deleteIfExists(uploadConfig.getUploadPath().resolve(fileName));
        } catch (IOException e) {
            // Log but don't fail the delete operation
        }
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
