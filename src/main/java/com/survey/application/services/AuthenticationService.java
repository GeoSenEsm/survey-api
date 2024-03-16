package com.survey.application.services;

import com.survey.application.dtos.LoginDto;
import java.util.concurrent.CompletableFuture;


public interface AuthenticationService {
    String getJwtToken(LoginDto dto);
}
