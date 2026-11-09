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

import java.util.Set;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "BOOK",
            "NOTE",
            "QUESTION",
            "ASSIGNMENT",
            "LAB",
            "SLIDES",
            "OTHER"
    );

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
            String courseCode,
            String courseName,
            String shortName,
            Integer academicYear,
            String category,
            User uploader) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file.");
        }

        // Validate course code
        String normalizedCourseCode = courseCode == null
                ? ""
                : courseCode.trim().toUpperCase();

        if (!normalizedCourseCode.matches("^ICT\\d{4}$")) {
            throw new IllegalArgumentException(
                    "Course code must follow ICTXXXX format (e.g. ICT4104)"
            );
        }

        // Validate category
        String normalizedCategory = category == null
                ? ""
                : category.trim().toUpperCase();

        if (!ALLOWED_CATEGORIES.contains(normalizedCategory)) {
            throw new IllegalArgumentException("Please select a valid category.");
        }

        if (courseName == null || courseName.isBlank()
                || shortName == null || shortName.isBlank()) {
            throw new IllegalArgumentException("Course name and short name are required.");
        }

        if (academicYear == null || academicYear < 2000) {
            throw new IllegalArgumentException("Please enter a valid academic year.");
        }

        // Find existing course or create a new one
        Course course = courseRepository.findByCourseCode(normalizedCourseCode)
                .orElseGet(() -> courseRepository.save(
                        new Course(
                                normalizedCourseCode,
                                courseName.trim(),
                                shortName.trim()
                        )
                ));

        // Map categories to storage directory names
        String categoryFolder = switch (normalizedCategory) {
            case "BOOK" -> "books";
            case "NOTE" -> "notes";
            case "QUESTION" -> "questions";
            case "ASSIGNMENT" -> "assignments";
            case "LAB" -> "labs";
            case "SLIDES" -> "slides";
            case "OTHER" -> "other";
            default -> throw new IllegalArgumentException("Invalid category");
        };

        // Build storage path: category/courseCode/filename
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        String filename = academicYear + "_" + course.getShortName() + "." + extension;

        String requestedPath = categoryFolder + "/"
                + course.getCourseCode() + "/"
                + filename;

        String storageKey = fileStorageService.store(file, requestedPath);

        try {
            Document document = new Document();

            // Generate title from original filename, without extension
            String generatedTitle = originalFilename == null
                    ? "Untitled document"
                    : originalFilename.replaceFirst("\\.[^.]+$", "");

            document.setTitle(generatedTitle);
            document.setCategory(normalizedCategory);
            document.setOriginalFilename(originalFilename);
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

        return filename.substring(filename.lastIndexOf(".") + 1)
                .toLowerCase();
    }
}