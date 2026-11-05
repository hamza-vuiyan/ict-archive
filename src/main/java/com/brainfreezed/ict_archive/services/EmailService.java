package com.brainfreezed.ict_archive.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String recipient, String verificationLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipient);
        message.setSubject("Verify Your ICT Archive Account");

        message.setText(
                "Hello,\n\n" +
                        "Thank you for registering with ICT Archive.\n\n" +
                        "Please click the link below to verify your email address:\n\n" +
                        verificationLink + "\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you did not create this account, please ignore this email.\n\n" +
                        "Regards,\nICT Archive Team"
        );

        mailSender.send(message);
    }
}