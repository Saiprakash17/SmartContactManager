package com.scm.contactmanager.services.impl;

import com.scm.contactmanager.entities.PasswordResetToken;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.PasswordResetTokenRepo;
import com.scm.contactmanager.services.PasswordResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private static final int EXPIRATION_MINUTES = 30;

    @Autowired
    private PasswordResetTokenRepo tokenRepo;

    @Override
    public PasswordResetToken createTokenForUser(User user) {
        // Remove existing token for user
        tokenRepo.findByUser(user).ifPresent(tokenRepo::delete);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();
        return tokenRepo.save(resetToken);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    @Override
    public boolean isTokenExpired(PasswordResetToken token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }

    @Override
    public void deleteToken(PasswordResetToken token) {
        tokenRepo.delete(token);
    }
} 