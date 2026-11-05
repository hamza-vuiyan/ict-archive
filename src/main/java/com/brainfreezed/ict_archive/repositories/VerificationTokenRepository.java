package com.brainfreezed.ict_archive.repositories;

import com.brainfreezed.ict_archive.entities.VerificationToken;
import com.brainfreezed.ict_archive.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHash(String tokenHash);

    Optional<VerificationToken> findByUser(User user);
}