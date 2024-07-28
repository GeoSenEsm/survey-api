package com.survey.application.services;

import com.survey.api.security.TokenProvider;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class ClaimsPrincipalServiceImpl implements ClaimsPrincipalService {

    private final TokenProvider tokenProvider;
    private final IdentityUserRepository identityUserRepository;

    @Autowired
    public ClaimsPrincipalServiceImpl(TokenProvider tokenProvider,
                                      IdentityUserRepository identityUserRepository) {
        this.tokenProvider = tokenProvider;
        this.identityUserRepository = identityUserRepository;
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

    public IdentityUser findIdentityUserFromToken(String token) {
        String usernameFromJwt = getCurrentUsernameIfExists(token);
        return identityUserRepository.findByUsername(usernameFromJwt)
                .orElseThrow(() -> new IllegalArgumentException("Invalid respondent ID - respondent doesn't exist"));
    }
}
