package com.survey.application.services;

public interface PasswordValidationService {
    void validate(String password);
    void validateOldPassword(String storedHash, String providedOldPassword);
}
