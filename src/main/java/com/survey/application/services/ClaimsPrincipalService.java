package com.survey.application.services;

public interface ClaimsPrincipalService {
    String getCurrentUsernameIfExists(String tokenBearerPrefix);
}
