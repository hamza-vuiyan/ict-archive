package com.brainfreezed.ict_archive.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestController
public class HomeController {
    @GetMapping("/health")
    public String health(){
        return "OK";
    }

    @GetMapping("/")
    public String home() {
         return "Welcome to the ICT Archive!";
    }
}
