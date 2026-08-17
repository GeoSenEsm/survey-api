package com.survey.api;

import com.survey.api.security.TokenProvider;
import com.survey.application.services.SensorTypeParameterService;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.models.SensorType;
import com.survey.domain.models.SensorTypeParameter;
import com.survey.domain.models.enums.SensorTypeCodes;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import com.survey.domain.repository.SensorTypeParameterRepository;
import com.survey.domain.repository.SensorTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class TestUtils {

    private final IdentityUserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final SensorTypeRepository sensorTypeRepository;
    private final SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    private final SensorTypeParameterRepository sensorTypeParameterRepository;
    private final SensorTypeParameterService sensorTypeParameterService;

    @Autowired
    public TestUtils(IdentityUserRepository userRepository, AuthenticationManager authenticationManager,
                     PasswordEncoder passwordEncoder, TokenProvider tokenProvider,
                     SensorTypeRepository sensorTypeRepository,
                     SensorParameterDefinitionRepository sensorParameterDefinitionRepository,
                     SensorTypeParameterRepository sensorTypeParameterRepository,
                     SensorTypeParameterService sensorTypeParameterService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.sensorTypeRepository = sensorTypeRepository;
        this.sensorParameterDefinitionRepository = sensorParameterDefinitionRepository;
        this.sensorTypeParameterRepository = sensorTypeParameterRepository;
        this.sensorTypeParameterService = sensorTypeParameterService;
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

    /**
     * Mirrors what installing the real "xiaomi" template does (raw temperature/humidity wired as
     * used parameters, each guaranteed a manual fallback) rather than just the bare sensor_type
     * row: several integration tests post sensor data with parameterCode "temperature"/"humidity"
     * for source "xiaomi", which is validated against the global "used sensor data" list
     * (SensorDataServiceImpl.toEntity), not against xiaomi's own raw catalog — since nothing is
     * pre-seeded any more, those codes must be created here or that POST 400s. Goes straight to
     * the repositories/ensureManualSource rather than the install endpoint so this stays usable
     * regardless of whether the initial survey has already been published in the calling test.
     * {@code @Transactional} keeps the sensor type and its raw-parameter rows in one persistence
     * context — without it, each repository call opens its own transaction/session, and the raw
     * parameter's non-nullable {@code sensorType} reference ends up pointing at an entity from an
     * already-closed session, which Hibernate treats as transient (TransientPropertyValueException)
     * regardless of the row already existing in the database.
     */
    @Transactional
    public SensorType getOrCreateXiaomiSensorType() {
        return sensorTypeRepository.findByCode(SensorTypeCodes.XIAOMI)
                .orElseGet(() -> {
                    SensorType xiaomi = new SensorType();
                    xiaomi.setId(UUID.randomUUID());
                    xiaomi.setCode(SensorTypeCodes.XIAOMI);
                    xiaomi.setName("Xiaomi");
                    xiaomi.setIntegrationMode("profile");
                    // Flush immediately: xiaomi's manually-assigned (non-generated) id means
                    // Spring Data routes save() through merge() rather than persist(), and without
                    // a flush here the raw parameter rows below would reference a sensor_type row
                    // Hibernate hasn't actually inserted yet (TransientPropertyValueException).
                    SensorType saved = sensorTypeRepository.saveAndFlush(xiaomi);
                    wireRawParameter(saved, "temperature", "Temperature", "C");
                    wireRawParameter(saved, "humidity", "Humidity", "%");
                    return saved;
                });
    }

    private void wireRawParameter(SensorType sensorType, String code, String name, String unit) {
        SensorParameterDefinition definition = sensorParameterDefinitionRepository.findByCode(code)
                .orElseGet(() -> {
                    SensorParameterDefinition created = new SensorParameterDefinition();
                    created.setCode(code);
                    created.setName(name);
                    created.setDataType("decimal");
                    created.setUnit(unit);
                    SensorParameterDefinition savedDefinition = sensorParameterDefinitionRepository.save(created);
                    sensorTypeParameterService.ensureManualSource(savedDefinition.getId());
                    return savedDefinition;
                });

        SensorTypeParameter rawParameter = new SensorTypeParameter();
        rawParameter.setSensorType(sensorType);
        rawParameter.setCode(code);
        rawParameter.setName(definition.getName());
        rawParameter.setDataType(definition.getDataType());
        rawParameter.setUnit(definition.getUnit());
        rawParameter.setUsedParameter(definition);
        sensorTypeParameterRepository.save(rawParameter);
    }
}
