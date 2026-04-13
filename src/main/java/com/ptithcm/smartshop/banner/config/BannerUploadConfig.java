package com.ptithcm.smartshop.banner.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class BannerUploadConfig {

    @Value("${banner.upload.dir:src/main/resources/static/uploads/banners}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    public Path getUploadPath() {
        return uploadPath;
    }
}
