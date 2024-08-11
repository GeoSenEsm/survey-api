package com.survey.application.services;

import com.survey.domain.models.IdentityUser;

public interface ClaimsPrincipalService {
    String getCurrentUsernameIfExists();
    IdentityUser findIdentityUser();
}
