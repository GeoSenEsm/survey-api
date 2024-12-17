package com.survey.application.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class PasswordValidationServiceImpl implements PasswordValidationService {
    private final PasswordEncoder passwordEncoder;

    public PasswordValidationServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void validateOldPassword(String storedHash, String providedOldPassword) {
        if (providedOldPassword == null || !passwordMatches(providedOldPassword, storedHash)) {
            throw new IllegalArgumentException("The provided old password does not match our records.");
        }
    }

    private boolean passwordMatches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
