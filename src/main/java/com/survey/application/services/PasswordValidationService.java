package com.survey.application.services;

public interface PasswordValidationService {
    void validateOldPassword(String storedHash, String providedOldPassword);
}
