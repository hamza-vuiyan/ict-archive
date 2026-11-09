package com.brainfreezed.ict_archive.controllers;

import com.brainfreezed.ict_archive.repositories.UserRepository;
import com.brainfreezed.ict_archive.services.DocumentService;
import com.brainfreezed.ict_archive.entities.User;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DocumentController {

    private final UserRepository userRepository;
    private final DocumentService documentService;

    public DocumentController(
            DocumentService documentService,
            UserRepository userRepository) {

        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/documents/upload")
    public String uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseCode") String courseCode,
            @RequestParam("courseName") String courseName,
            @RequestParam("shortName") String shortName,
            @RequestParam("academicYear") Integer academicYear,
            @RequestParam("category") String category,

            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User uploader = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            documentService.upload(
                    file,
                    courseCode,
                    courseName,
                    shortName,
                    academicYear,
                    category,
                    uploader
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Document uploaded successfully!"
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/documents/upload";
    }

    @GetMapping("/documents/upload")
    public String showUploadPage() {
        return "upload";
    }
}