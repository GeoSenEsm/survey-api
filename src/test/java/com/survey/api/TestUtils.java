package com.survey.api;

import com.survey.api.security.TokenProvider;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorType;
import com.survey.domain.models.enums.SensorTypeCodes;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestUtils {

    private final IdentityUserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final SensorTypeRepository sensorTypeRepository;

    @Autowired
    public TestUtils(IdentityUserRepository userRepository, AuthenticationManager authenticationManager,
                     PasswordEncoder passwordEncoder, TokenProvider tokenProvider,
                     SensorTypeRepository sensorTypeRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.sensorTypeRepository = sensorTypeRepository;
    }

    public IdentityUser createUserWithRole(String role, String password) {
        IdentityUser user = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole(role)
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode(password));

        return userRepository.saveAndFlush(user);
    }

    public String authenticateAndGenerateToken(IdentityUser user, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), password));
        return tokenProvider.generateToken(authentication);
    }

    public SensorType getOrCreateXiaomiSensorType() {
        return sensorTypeRepository.findByCode(SensorTypeCodes.XIAOMI)
                .orElseGet(() -> {
                    SensorType xiaomi = new SensorType();
                    xiaomi.setId(UUID.randomUUID());
                    xiaomi.setCode(SensorTypeCodes.XIAOMI);
                    xiaomi.setName("Xiaomi");
                    xiaomi.setIntegrationMode("profile");
                    return sensorTypeRepository.save(xiaomi);
                });
    }
}
