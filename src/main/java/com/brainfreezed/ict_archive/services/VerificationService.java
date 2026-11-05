
package com.brainfreezed.ict_archive.services;

import com.brainfreezed.ict_archive.entities.User;
import com.brainfreezed.ict_archive.entities.VerificationToken;
import com.brainfreezed.ict_archive.repositories.VerificationTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class VerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    public VerificationService(
            VerificationTokenRepository tokenRepository,
            EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createAndSendVerificationToken(User user) {

        // Generate a cryptographically secure random token
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Hash the token before storing it
        String tokenHash = hashToken(rawToken);

        // Replace any existing token for this user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        tokenRepository.flush();

        // Create and save the new verification token
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(
                OffsetDateTime.now().plusHours(24)
        );

        tokenRepository.save(verificationToken);

        // Send the original token through email
        String verificationLink = baseUrl + "/verify?token=" + rawToken;

        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationLink
        );
    }

    @Transactional
    public boolean verifyToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        // Hash the received token
        String tokenHash = hashToken(rawToken);

        var tokenOptional = tokenRepository.findByTokenHash(tokenHash);

        if (tokenOptional.isEmpty()) {
            return false;
        }

        VerificationToken token = tokenOptional.get();

        // Reject expired tokens
        if (!token.getExpiryDate().isAfter(OffsetDateTime.now())) {
            tokenRepository.delete(token);
            return false;
        }

        // Enable the associated user
        User user = token.getUser();
        user.setEnabled(true);

        // Delete the token to prevent reuse
        tokenRepository.delete(token);

        return true;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not generate token hash", e
            );
        }
    }
}
