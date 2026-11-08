package com.brainfreezed.ict_archive.services.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file, String relativePath);

    Resource load(String storageKey);

    void delete(String storageKey);
}