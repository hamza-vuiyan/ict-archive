package com.brainfreezed.ict_archive.controllers;

import com.brainfreezed.ict_archive.services.RegistrationService;
import com.brainfreezed.ict_archive.services.VerificationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.brainfreezed.ict_archive.dto.RegistrationRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final RegistrationService registrationService;
    private final VerificationService verificationService;

    public HomeController(
            RegistrationService registrationService,
            VerificationService verificationService) {

        this.registrationService = registrationService;
        this.verificationService = verificationService;
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "Welcome to the ICT Archive!";
    }

    @GetMapping("/health") // New endpoint for health check
    @ResponseBody
    public String health(){
        return "OK";
    }

    @GetMapping("/register") // New endpoint for registration page
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            registrationService.registerUser(request);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Registration successful! Please verify your email."
            );
            return "redirect:/register";

        } catch (IllegalArgumentException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verifyEmail(
            @RequestParam("token") String token,
            Model model) {

        boolean verified = verificationService.verifyToken(token);

        if (verified) {
            model.addAttribute("success", true);
            model.addAttribute("message",
                    "Your email has been verified successfully! You can now log in.");
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message",
                    "Invalid, expired, or already-used verification link.");
        }

        return "verification-result";
    }

    @GetMapping("/login") //login page endpoint
    public String showLoginPage() {
        return "login";
    }

}
