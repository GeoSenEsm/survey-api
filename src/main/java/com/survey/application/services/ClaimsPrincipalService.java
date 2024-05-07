package com.survey.application.services;

public interface ClaimsPrincipalService {
    String getCurrentUsername(String tokenBearerPrefix);
}
