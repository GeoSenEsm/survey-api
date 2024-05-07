package com.survey.application.services;

import com.survey.api.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClaimsPrincipalServiceImpl implements ClaimsPrincipalService{

    private final TokenProvider tokenProvider;

    @Autowired
    public ClaimsPrincipalServiceImpl(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String getCurrentUsername(String tokenBearerPrefix) {
        if (tokenBearerPrefix == null) {
            return null;
        }
        String token = tokenBearerPrefix.substring(7);
        if (tokenProvider.validateToken(token)) {
            return tokenProvider.getUsernameFromJwt(token);
        } else {
            return null;
        }
    }
}
