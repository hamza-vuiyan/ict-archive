package com.brainfreezed.ict_archive.controllers;

import com.brainfreezed.ict_archive.services.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import com.brainfreezed.ict_archive.dto.RegistrationRequest;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class HomeController {

    private final RegistrationService registrationService;

    public HomeController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/health") // New endpoint for health check
    public String health(){
        return "OK";
    }

    @GetMapping("/")  // New endpoint for the home page
    public String home() {
        return "Welcome to the ICT Archive! 🚀 CI/CD is working!";
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


}
