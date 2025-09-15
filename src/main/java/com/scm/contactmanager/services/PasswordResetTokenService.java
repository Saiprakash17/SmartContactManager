package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.PasswordResetToken;
import com.scm.contactmanager.entities.User;

import java.util.Optional;

public interface PasswordResetTokenService {
    PasswordResetToken createTokenForUser(User user);
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findValidTokenForUser(User user);
    boolean isTokenExpired(PasswordResetToken token);
    void deleteToken(PasswordResetToken token);
} 