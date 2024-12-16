package com.survey.application.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
@Service
public class PasswordValidationServiceImpl implements PasswordValidationService {
    private final PasswordEncoder passwordEncoder;
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*].*");

    public PasswordValidationServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long.");
        }
        if (!UPPERCASE.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!LOWERCASE.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!DIGIT.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
        if (!SPECIAL_CHAR.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one special character.");
        }
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
