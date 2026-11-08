package com.brainfreezed.ict_archive.services.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalFileStorageService(
            @Value("${app.storage.location}") String storageLocation) {

        this.rootLocation = Paths.get(storageLocation)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String store(MultipartFile file, String relativePath) {

        try {
            Path destination = rootLocation.resolve(relativePath).normalize();

            if (!destination.startsWith(rootLocation)) {
                throw new RuntimeException("Invalid file path");
            }

            Files.createDirectories(destination.getParent());

            String filename = destination.getFileName().toString();
            String extension = "";
            String name = filename;

            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex > 0) {
                name = filename.substring(0, dotIndex);
                extension = filename.substring(dotIndex);
            }

            int counter = 1;

            while (true) {
                Path candidate = destination.getParent()
                        .resolve(name + (counter == 1 ? "" : "_" + counter) + extension);

                try {
                    Files.copy(file.getInputStream(), candidate);

                    return rootLocation.relativize(candidate)
                            .toString()
                            .replace("\\", "/");

                } catch (FileAlreadyExistsException e) {
                    counter++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Resource load(String storageKey) {

        try {
            Path filePath = rootLocation.resolve(storageKey).normalize();

            if (!filePath.startsWith(rootLocation)) {
                throw new RuntimeException("Invalid storage key");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new RuntimeException("File not found: " + storageKey);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load file", e);
        }
    }

    @Override
    public void delete(String storageKey) {

        try {
            Path filePath = rootLocation.resolve(storageKey).normalize();

            if (!filePath.startsWith(rootLocation)) {
                throw new RuntimeException("Invalid storage key");
            }

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}