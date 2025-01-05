package com.survey.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import io.jsonwebtoken.Jwts;


@Component
public class TokenProvider {

    private final SecuritySettings securitySettings;

    @Autowired
    public TokenProvider(SecuritySettings securitySettings) {
        this.securitySettings = securitySettings;
    }

    public String generateToken(Authentication authentication){
        String username = authentication.getName();
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);

        int expirationDays = securitySettings.getExpiration();
        OffsetDateTime expireDateTime = currentDateTime.plusDays(expirationDays);

        Date issuedAt = Date.from(currentDateTime.toInstant());
        Date expireDate = Date.from(expireDateTime.toInstant());

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(issuedAt)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, securitySettings.getKey())
                .compact();


        return token;
    }

    public String getUsernameFromJwt(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(securitySettings.getKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(securitySettings.getKey())
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e){
            throw new AuthenticationCredentialsNotFoundException("Jwt was expired or incorrect");
        }
    }
}
