package com.survey.application.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class RandomCredentialsGenerator implements CredentialsGenerator{
    private static final int PASSWORD_LENGTH = 8;
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";


    @Override
    public String getRandomPassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHARACTERS.length());
            char randomChar = PASSWORD_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
