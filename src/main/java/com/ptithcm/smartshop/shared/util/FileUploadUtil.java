package com.ptithcm.smartshop.shared.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Utility for handling file uploads. Generates UUID-based filenames
 * and manages file storage in a configured directory.
 */
public final class FileUploadUtil {

    private FileUploadUtil() {
    }

    /**
     * Save a multipart file to the given directory with a UUID-based name.
     *
     * @param file        the uploaded file
     * @param uploadDir   the target directory path (will be created if missing)
     * @param urlPrefix   the URL prefix to prepend to the stored filename (e.g. "/uploads/banners")
     * @return the relative URL path to the saved file
     */
    public static String saveFile(MultipartFile file, Path uploadDir, String urlPrefix) {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + fileName, e);
        }

        return urlPrefix + "/" + fileName;
    }

    /**
     * Delete a previously uploaded file by its relative URL path.
     *
     * @param relativePath the stored relative path (e.g. "/uploads/banners/abc.jpg")
     * @param uploadDir    the base upload directory
     * @param urlPrefix    the URL prefix used when saving (e.g. "/uploads/banners")
     */
    public static void deleteFile(String relativePath, Path uploadDir, String urlPrefix) {
        if (relativePath == null || !relativePath.startsWith(urlPrefix)) {
            return;
        }
        String fileName = relativePath.substring(urlPrefix.length() + 1);
        try {
            Files.deleteIfExists(uploadDir.resolve(fileName));
        } catch (IOException e) {
            // Log but don't fail the delete operation
        }
    }

    private static String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
