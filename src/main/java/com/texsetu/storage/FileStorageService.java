package com.texsetu.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file, String subFolder) throws IOException;
    byte[] loadFileAsBytes(String fileName) throws IOException;
    boolean deleteFile(String fileUrl);
    void validateFile(MultipartFile file) throws IllegalArgumentException;
}
