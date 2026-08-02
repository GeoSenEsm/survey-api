package com.survey.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.MobileSensorDeviceSecretsDto;
import com.survey.domain.models.RespondentSensorAssignment;
import com.survey.domain.models.SensorDeviceSecret;
import com.survey.domain.models.SensorGattProfile;
import com.survey.domain.models.SensorMac;
import com.survey.domain.repository.RespondentSensorAssignmentRepository;
import com.survey.domain.repository.SensorDeviceSecretRepository;
import com.survey.domain.repository.SensorGattProfileRepository;
import com.survey.domain.repository.SensorMacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class SensorDeviceSecretService {
    private final SensorDeviceSecretRepository secretRepository;
    private final SensorMacRepository sensorMacRepository;
    private final SensorGattProfileRepository profileRepository;
    private final RespondentSensorAssignmentRepository assignmentRepository;
    private final SensorSecretCrypto crypto;
    private final ObjectMapper objectMapper;

    public SensorDeviceSecretService(
            SensorDeviceSecretRepository secretRepository,
            SensorMacRepository sensorMacRepository,
            SensorGattProfileRepository profileRepository,
            RespondentSensorAssignmentRepository assignmentRepository,
            SensorSecretCrypto crypto,
            ObjectMapper objectMapper) {
        this.secretRepository = secretRepository;
        this.sensorMacRepository = sensorMacRepository;
        this.profileRepository = profileRepository;
        this.assignmentRepository = assignmentRepository;
        this.crypto = crypto;
        this.objectMapper = objectMapper;
    }

    public void put(UUID sensorMacId, String secretName, String value) {
        SensorMac sensor = sensorMacRepository.findById(sensorMacId)
                .orElseThrow(() -> new NoSuchElementException("Sensor device was not found: " + sensorMacId));
        Set<String> required = requiredSecrets(sensor.getSensorTypeId());
        if (!required.contains(secretName)) {
            throw new IllegalArgumentException(
                    "Secret " + secretName + " is not required by the device's published profile.");
        }
        SensorSecretCrypto.EncryptedSecret encrypted = crypto.encrypt(sensorMacId, secretName, value);
        SensorDeviceSecret secret = secretRepository.findBySensorMacIdAndSecretName(sensorMacId, secretName)
                .orElseGet(SensorDeviceSecret::new);
        secret.setSensorMac(sensor);
        secret.setSecretName(secretName);
        secret.setNonce(encrypted.nonce());
        secret.setCiphertext(encrypted.ciphertext());
        secret.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC).withNano(0));
        secretRepository.save(secret);
    }

    @Transactional(readOnly = true)
    public List<MobileSensorDeviceSecretsDto> getForRespondent(UUID respondentId) {
        List<RespondentSensorAssignment> assignments =
                assignmentRepository.findByRespondentIdAndEnabledTrueOrderByPriorityOrder(respondentId);
        Map<UUID, Set<String>> requiredBySensor = new HashMap<>();
        for (RespondentSensorAssignment assignment : assignments) {
            if (assignment.getSensorMac() != null) {
                requiredBySensor.put(
                        assignment.getSensorMac().getId(),
                        requiredSecrets(assignment.getSensorType().getId()));
            }
        }
        if (requiredBySensor.isEmpty()) {
            return List.of();
        }

        Map<UUID, Map<String, String>> decrypted = new HashMap<>();
        for (SensorDeviceSecret secret : secretRepository.findBySensorMacIdIn(requiredBySensor.keySet())) {
            UUID sensorId = secret.getSensorMac().getId();
            if (requiredBySensor.getOrDefault(sensorId, Set.of()).contains(secret.getSecretName())) {
                decrypted.computeIfAbsent(sensorId, ignored -> new HashMap<>())
                        .put(secret.getSecretName(), crypto.decrypt(
                                sensorId, secret.getSecretName(), secret.getNonce(), secret.getCiphertext()));
            }
        }
        List<MobileSensorDeviceSecretsDto> result = new ArrayList<>();
        decrypted.forEach((sensorId, secrets) ->
                result.add(new MobileSensorDeviceSecretsDto(sensorId, Map.copyOf(secrets))));
        return List.copyOf(result);
    }

    /**
     * Names of secrets the sensor type's currently published profile declares via
     * {@code requiredSecrets}, or an empty set if the type has no published profile (e.g. manual/none
     * integration modes, or a native adapter). Used by admin UIs to decide whether a secret input
     * (like a bind key) should be shown at all for a given type, before {@link #put} would reject it.
     */
    @Transactional(readOnly = true)
    public Set<String> listRequiredSecrets(UUID sensorTypeId) {
        return profileRepository.findBySensorTypeIdAndStatus(sensorTypeId, "published")
                .map(this::parseRequiredSecrets)
                .orElseGet(Set::of);
    }

    private Set<String> requiredSecrets(UUID sensorTypeId) {
        SensorGattProfile profile = profileRepository.findBySensorTypeIdAndStatus(sensorTypeId, "published")
                .orElseThrow(() -> new IllegalStateException("The sensor type has no published profile."));
        return parseRequiredSecrets(profile);
    }

    private Set<String> parseRequiredSecrets(SensorGattProfile profile) {
        try {
            JsonNode names = objectMapper.readTree(profile.getSpecJson()).path("requiredSecrets");
            Set<String> required = new HashSet<>();
            names.forEach(name -> required.add(name.asText()));
            return required;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored BLE profile JSON is invalid.", exception);
        }
    }
}
