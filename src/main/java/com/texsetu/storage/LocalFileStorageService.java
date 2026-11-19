package com.texsetu.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "texsetu.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "application/pdf", "text/csv", "text/plain"
    );

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${texsetu.storage.local-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Initialized LocalFileStorageService at directory: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("Could not create local storage directory: {}", this.rootLocation, e);
            throw new RuntimeException("Could not initialize local file storage", e);
        }
    }

    @Override
    public void validateFile(MultipartFile file) throws IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum permitted limit of 10MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType + ". Permitted: JPG, PNG, WEBP, PDF, CSV, TXT.");
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // Sanitize and generate secure random filename
        String safeName = UUID.randomUUID().toString() + extension;

        Path targetDir = this.rootLocation.resolve(subFolder).normalize();
        if (!targetDir.startsWith(this.rootLocation)) {
            throw new SecurityException("Directory traversal attempt detected.");
        }
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(safeName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File stored successfully: {} in subfolder: {}", safeName, subFolder);

        return "/uploads/" + subFolder + "/" + safeName;
    }

    @Override
    public byte[] loadFileAsBytes(String relativePath) throws IOException {
        String cleanPath = relativePath.startsWith("/uploads/") ? relativePath.substring(9) : relativePath;
        Path filePath = this.rootLocation.resolve(cleanPath).normalize();
        if (!filePath.startsWith(this.rootLocation) || !Files.exists(filePath)) {
            throw new NoSuchFileException("File not found or access denied: " + relativePath);
        }
        return Files.readAllBytes(filePath);
    }

    @Override
    public boolean deleteFile(String relativePath) {
        try {
            String cleanPath = relativePath.startsWith("/uploads/") ? relativePath.substring(9) : relativePath;
            Path filePath = this.rootLocation.resolve(cleanPath).normalize();
            if (filePath.startsWith(this.rootLocation)) {
                return Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", relativePath, e);
        }
        return false;
    }
}
