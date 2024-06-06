package com.survey.application.services;

import com.survey.api.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class ClaimsPrincipalServiceImpl implements ClaimsPrincipalService{

    private final TokenProvider tokenProvider;

    @Autowired
    public ClaimsPrincipalServiceImpl(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }


    private String getCurrentUsernameFromToken(String tokenBearerPrefix) {
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
    public String getCurrentUsernameIfExists(String token){
        String usernameFromJwt = getCurrentUsernameFromToken(token);
        if (usernameFromJwt == null){
            throw new BadCredentialsException("Invalid credentials");
        }
        return usernameFromJwt;
    }
}
