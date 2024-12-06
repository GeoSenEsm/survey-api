package com.survey.application.services;

import com.survey.api.security.TokenProvider;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class ClaimsPrincipalServiceImpl implements ClaimsPrincipalService {

    private final TokenProvider tokenProvider;
    private final IdentityUserRepository identityUserRepository;
    private final HttpServletRequest request;

    @Autowired
    public ClaimsPrincipalServiceImpl(TokenProvider tokenProvider,
                                      IdentityUserRepository identityUserRepository,
                                      HttpServletRequest request) {
        this.tokenProvider = tokenProvider;
        this.identityUserRepository = identityUserRepository;
        this.request = request;
    }

    private String getCurrentUsernameFromToken() {
        String token = (String) request.getAttribute("AuthorizationToken");
        if (token == null) {
            return null;
        }
        if (tokenProvider.validateToken(token)) {
            return tokenProvider.getUsernameFromJwt(token);
        } else {
            return null;
        }
    }

    public String getCurrentUsernameIfExists() {
        String usernameFromJwt = getCurrentUsernameFromToken();
        if (usernameFromJwt == null) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return usernameFromJwt;
    }

    public IdentityUser findIdentityUser() {
        String usernameFromJwt = getCurrentUsernameIfExists();
        return identityUserRepository.findByUsername(usernameFromJwt)
                .orElseThrow(() -> new IllegalArgumentException("Invalid respondent ID - respondent doesn't exist"));
    }

    public boolean isAnonymous() {
        return getCurrentUsernameFromToken() == null;
    }
}
