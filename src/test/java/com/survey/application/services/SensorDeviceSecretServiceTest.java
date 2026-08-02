package com.survey.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.MobileSensorDeviceSecretsDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentSensorAssignment;
import com.survey.domain.models.SensorDeviceSecret;
import com.survey.domain.models.SensorGattProfile;
import com.survey.domain.models.SensorMac;
import com.survey.domain.models.SensorType;
import com.survey.domain.repository.RespondentSensorAssignmentRepository;
import com.survey.domain.repository.SensorDeviceSecretRepository;
import com.survey.domain.repository.SensorGattProfileRepository;
import com.survey.domain.repository.SensorMacRepository;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SensorDeviceSecretServiceTest {
    private static final String SECRET = "00112233445566778899AABBCCDDEEFF";
    private static final String KEY = Base64.getEncoder()
            .encodeToString("0123456789ABCDEF0123456789ABCDEF".getBytes(StandardCharsets.UTF_8));

    @Test
    void getForRespondent_decryptsOnlySecretsForAssignedPhysicalDevices() {
        SensorDeviceSecretRepository secretRepository = mock(SensorDeviceSecretRepository.class);
        SensorMacRepository sensorRepository = mock(SensorMacRepository.class);
        SensorGattProfileRepository profileRepository = mock(SensorGattProfileRepository.class);
        RespondentSensorAssignmentRepository assignmentRepository =
                mock(RespondentSensorAssignmentRepository.class);
        SensorSecretCrypto crypto = new SensorSecretCrypto(KEY);

        UUID respondentId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        SensorType type = new SensorType();
        type.setId(UUID.randomUUID());
        SensorMac sensor = new SensorMac();
        sensor.setId(sensorId);
        RespondentSensorAssignment assignment = new RespondentSensorAssignment();
        IdentityUser respondent = new IdentityUser();
        respondent.setId(respondentId);
        assignment.setRespondent(respondent);
        assignment.setSensorType(type);
        assignment.setSensorMac(sensor);
        assignment.setEnabled(true);

        SensorGattProfile profile = new SensorGattProfile();
        profile.setSensorType(type);
        profile.setSpecJson("{\"requiredSecrets\":[\"bind_key\"]}");
        SensorSecretCrypto.EncryptedSecret encrypted = crypto.encrypt(sensorId, "bind_key", SECRET);
        SensorDeviceSecret stored = new SensorDeviceSecret();
        stored.setSensorMac(sensor);
        stored.setSecretName("bind_key");
        stored.setNonce(encrypted.nonce());
        stored.setCiphertext(encrypted.ciphertext());

        when(assignmentRepository.findByRespondentIdAndEnabledTrueOrderByPriorityOrder(respondentId))
                .thenReturn(List.of(assignment));
        when(profileRepository.findBySensorTypeIdAndStatus(type.getId(), "published"))
                .thenReturn(Optional.of(profile));
        when(secretRepository.findBySensorMacIdIn(Set.of(sensorId))).thenReturn(List.of(stored));

        SensorDeviceSecretService service = new SensorDeviceSecretService(
                secretRepository, sensorRepository, profileRepository, assignmentRepository, crypto, new ObjectMapper());
        List<MobileSensorDeviceSecretsDto> result = service.getForRespondent(respondentId);

        assertThat(result).singleElement().satisfies(device -> {
            assertThat(device.sensorMacId()).isEqualTo(sensorId);
            assertThat(device.secrets()).containsEntry("bind_key", SECRET);
        });
    }

    @Test
    void listRequiredSecrets_readsNamesFromThePublishedProfile() {
        SensorGattProfileRepository profileRepository = mock(SensorGattProfileRepository.class);
        UUID sensorTypeId = UUID.randomUUID();
        SensorGattProfile profile = new SensorGattProfile();
        profile.setSpecJson("{\"requiredSecrets\":[\"bind_key\"]}");
        when(profileRepository.findBySensorTypeIdAndStatus(sensorTypeId, "published"))
                .thenReturn(Optional.of(profile));

        SensorDeviceSecretService service = new SensorDeviceSecretService(
                mock(SensorDeviceSecretRepository.class), mock(SensorMacRepository.class), profileRepository,
                mock(RespondentSensorAssignmentRepository.class), new SensorSecretCrypto(KEY), new ObjectMapper());

        assertThat(service.listRequiredSecrets(sensorTypeId)).containsExactly("bind_key");
    }

    @Test
    void listRequiredSecrets_isEmptyWhenTheTypeHasNoPublishedProfile() {
        SensorGattProfileRepository profileRepository = mock(SensorGattProfileRepository.class);
        UUID sensorTypeId = UUID.randomUUID();
        when(profileRepository.findBySensorTypeIdAndStatus(sensorTypeId, "published"))
                .thenReturn(Optional.empty());

        SensorDeviceSecretService service = new SensorDeviceSecretService(
                mock(SensorDeviceSecretRepository.class), mock(SensorMacRepository.class), profileRepository,
                mock(RespondentSensorAssignmentRepository.class), new SensorSecretCrypto(KEY), new ObjectMapper());

        assertThat(service.listRequiredSecrets(sensorTypeId)).isEmpty();
    }
}
