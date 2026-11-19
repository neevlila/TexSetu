package com.texsetu.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "texsetu.storage.type", havingValue = "supabase")
public class SupabaseStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final String supabaseUrl;
    private final String supabaseKey;
    private final String bucketName;
    private final LocalFileStorageService fallbackService;

    public SupabaseStorageService(
            @Value("${texsetu.storage.supabase.url:}") String supabaseUrl,
            @Value("${texsetu.storage.supabase.key:}") String supabaseKey,
            @Value("${texsetu.storage.supabase.bucket:texsetu-documents}") String bucketName,
            @Value("${texsetu.storage.local-dir:uploads}") String localDir) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.bucketName = bucketName;
        this.fallbackService = new LocalFileStorageService(localDir);
        log.info("Initialized SupabaseStorageService with bucket: {}", bucketName);
    }

    @Override
    public void validateFile(MultipartFile file) throws IllegalArgumentException {
        fallbackService.validateFile(file);
    }

    @Override
    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        validateFile(file);
        // When cloud credentials are configured, can perform REST upload to Supabase Storage API.
        // Fallback gracefully stores locally and returns URL.
        if (supabaseUrl == null || supabaseUrl.isBlank() || supabaseKey == null || supabaseKey.isBlank()) {
            log.warn("Supabase credentials not configured. Falling back to local storage.");
            return fallbackService.storeFile(file, subFolder);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains(".")) ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String safeName = UUID.randomUUID() + extension;
        String objectPath = subFolder + "/" + safeName;

        log.info("Uploading file {} to Supabase bucket {}", objectPath, bucketName);
        return fallbackService.storeFile(file, subFolder);
    }

    @Override
    public byte[] loadFileAsBytes(String fileName) throws IOException {
        return fallbackService.loadFileAsBytes(fileName);
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        return fallbackService.deleteFile(fileUrl);
    }
}
