package com.brainfreezed.ict_archive.services;

import com.brainfreezed.ict_archive.entities.Course;
import com.brainfreezed.ict_archive.entities.Document;
import com.brainfreezed.ict_archive.entities.User;
import com.brainfreezed.ict_archive.repositories.CourseRepository;
import com.brainfreezed.ict_archive.repositories.DocumentRepository;
import com.brainfreezed.ict_archive.services.storage.FileStorageService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

    public DocumentService(
            DocumentRepository documentRepository,
            CourseRepository courseRepository,
            FileStorageService fileStorageService) {

        this.documentRepository = documentRepository;
        this.courseRepository = courseRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Document upload(
            MultipartFile file,
            String title,
            String description,
            String courseCode,
            String courseName,
            String shortName,
            Integer academicYear,
            User uploader) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseGet(() -> courseRepository.save(
                        new Course(courseCode, courseName, shortName)
                ));

        String folderName = course.getCourseCode() + "-"
                + course.getCourseName().trim().replaceAll("[^a-zA-Z0-9]+", "_");

        String filename = academicYear + "_" + course.getShortName() + "."
                + getExtension(file.getOriginalFilename());

        String requestedPath = folderName + "/" + filename;

        String storageKey = fileStorageService.store(file, requestedPath);

        try {
            Document document = new Document();

            document.setTitle(title);
            document.setDescription(description);
            document.setOriginalFilename(file.getOriginalFilename());
            document.setStorageKey(storageKey);
            document.setStorageProvider("LOCAL");
            document.setContentType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setUploadedBy(uploader);
            document.setCourse(course);
            document.setAcademicYear(academicYear);

            return documentRepository.save(document);

        } catch (RuntimeException e) {
            fileStorageService.delete(storageKey);
            throw e;
        }
    }

    private String getExtension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return "file";
        }

        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}