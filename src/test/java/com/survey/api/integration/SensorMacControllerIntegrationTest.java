package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;
import com.survey.application.dtos.UpdatedSensorMacDtoIn;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorMac;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorMacRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class SensorMacControllerIntegrationTest {
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private static final String RESPONDENT_PASSWORD = "testRespondentPassword";

    private static final String VALID_SENSOR_ID_1 = "100";
    private static final String VALID_SENSOR_MAC_1 = "11:22:33:44:55:66";
    private static final String VALID_SENSOR_ID_2 = "200";
    private static final String VALID_SENSOR_MAC_2 = "aa:bb:cc:dd:ee:ff";
    private static final String VALID_UPDATED_SENSOR_MAC_2 = "aa:80:15:dd:ee:ff";
    private static final String VALID_SENSOR_ID_3 = "300";
    private static final String VALID_SENSOR_MAC_3 = "A1:C2:D4:44:80:01";

    private static final String INVALID_MAC = "this is not mac";

    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final TestUtils testUtils;
    private final SensorMacRepository sensorMacRepository;

    @Autowired
    public SensorMacControllerIntegrationTest(WebTestClient webTestClient, IdentityUserRepository userRepository, TestUtils testUtils, SensorMacRepository sensorMacRepository) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.testUtils = testUtils;
        this.sensorMacRepository = sensorMacRepository;
    }

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
        sensorMacRepository.deleteAll();
    }

    @Test
    void saveSensorMacList_validData_shouldReturnCreatedStatus(){
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        List<SensorMacDtoIn> sensorMacDtoInList = getValidSensorMacDtoList();

        var response = webTestClient.post()
                .uri("/api/sensormac")
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(sensorMacDtoInList)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(SensorMacDtoOut.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(3);

        assertThat(response.get(0).getId()).isNotNull();
        assertThat(response.get(0).getSensorId()).isEqualTo(VALID_SENSOR_ID_1);
        assertThat(response.get(0).getSensorMac()).isEqualTo(VALID_SENSOR_MAC_1.toUpperCase());
        assertThat(response.get(0).getRowVersion()).isNotNull();

        assertThat(response.get(1).getId()).isNotNull();
        assertThat(response.get(1).getSensorId()).isEqualTo(VALID_SENSOR_ID_2);
        assertThat(response.get(1).getSensorMac()).isEqualTo(VALID_SENSOR_MAC_2.toUpperCase());
        assertThat(response.get(1).getRowVersion()).isNotNull();

        assertThat(response.get(2).getId()).isNotNull();
        assertThat(response.get(2).getSensorId()).isEqualTo(VALID_SENSOR_ID_3);
        assertThat(response.get(2).getSensorMac()).isEqualTo(VALID_SENSOR_MAC_3.toUpperCase());
        assertThat(response.get(2).getRowVersion()).isNotNull();
    }

    @Test
    void saveSensorMacList_duplicateSensorIds_shouldReturnConflict() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        List<SensorMacDtoIn> duplicateSensorMacDtoList = List.of(
                new SensorMacDtoIn(VALID_SENSOR_ID_1, VALID_SENSOR_MAC_1),
                new SensorMacDtoIn(VALID_SENSOR_ID_1, VALID_SENSOR_MAC_2)
        );

        webTestClient.post()
                .uri("/api/sensormac")
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(duplicateSensorMacDtoList)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void saveSensorMacList_duplicateSensorMacs_shouldReturnConflict() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        List<SensorMacDtoIn> duplicateSensorMacDtoList = List.of(
                new SensorMacDtoIn(VALID_SENSOR_ID_1, VALID_SENSOR_MAC_1),
                new SensorMacDtoIn(VALID_SENSOR_ID_2, VALID_SENSOR_MAC_1)
        );

        webTestClient.post()
                .uri("/api/sensormac")
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(duplicateSensorMacDtoList)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void saveSensorMacList_invalidMac_shouldReturnBadRequest() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        List<SensorMacDtoIn> invalidSensorMacDtoList = List.of(
                new SensorMacDtoIn(VALID_SENSOR_ID_1, INVALID_MAC)
        );

        webTestClient.post()
                .uri("/api/sensormac")
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(invalidSensorMacDtoList)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateSensorMac_validData_shouldReturnOkStatus(){
        saveSensorMacListDirectly(getValidSensorMacDtoList());
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        UpdatedSensorMacDtoIn updatedSensorMacDtoIn = new UpdatedSensorMacDtoIn(VALID_UPDATED_SENSOR_MAC_2);

        var response = webTestClient.put()
                .uri("/api/sensormac/{VALID_SENSOR_ID_2}", VALID_SENSOR_ID_2)
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(updatedSensorMacDtoIn)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorMacDtoOut.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        assertThat(response.get(0).getId()).isNotNull();
        assertThat(response.get(0).getSensorId()).isEqualTo(VALID_SENSOR_ID_2);
        assertThat(response.get(0).getSensorMac()).isEqualTo(VALID_UPDATED_SENSOR_MAC_2.toUpperCase());
        assertThat(response.get(0).getRowVersion()).isNotNull();
    }

    @Test
    void updateSensorMac_nonExistingSensorId_shouldReturnNotFound() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        UpdatedSensorMacDtoIn updatedSensorMacDtoIn = new UpdatedSensorMacDtoIn(VALID_UPDATED_SENSOR_MAC_2);

        webTestClient.put()
                .uri("/api/sensormac/nonexistent-sensor-id")
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(updatedSensorMacDtoIn)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAll_whenNoRecordsExist_shouldReturnEmptyList() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        var response = webTestClient.get()
                .uri("/api/sensormac/all")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorMacDtoOut.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    void getAll_shouldReturnOkStatus(){
        saveSensorMacListDirectly(getValidSensorMacDtoList());
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        var response = webTestClient.get()
                .uri("/api/sensormac/all")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorMacDtoOut.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(3);

        assertThat(response.get(0).getId()).isNotNull();
        assertThat(response.get(0).getSensorId()).isEqualTo(VALID_SENSOR_ID_1);
        assertThat(response.get(0).getSensorMac()).isEqualTo(VALID_SENSOR_MAC_1.toUpperCase());
        assertThat(response.get(0).getRowVersion()).isNotNull();

        assertThat(response.get(1).getId()).isNotNull();
        assertThat(response.get(1).getSensorId()).isEqualTo(VALID_SENSOR_ID_2);
        assertThat(response.get(1).getSensorMac()).isEqualTo(VALID_SENSOR_MAC_2.toUpperCase());
        assertThat(response.get(1).getRowVersion()).isNotNull();

        assertThat(response.get(2).getId()).isNotNull();
        assertThat(response.get(2).getSensorId()).isEqualTo(VALID_SENSOR_ID_3);
        assertThat(response.get(2).getSensorMac()).isEqualTo(VALID_SENSOR_MAC_3.toUpperCase());
        assertThat(response.get(2).getRowVersion()).isNotNull();
    }

    @Test
    void getSpecificSensorMac_shouldReturnOkStatus(){
        saveSensorMacListDirectly(getValidSensorMacDtoList());
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensormac")
                        .queryParam("sensorId", VALID_SENSOR_ID_2)
                        .build())
                .header("Authorization", "Bearer " + respondentToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorMacDtoOut.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        assertThat(response.get(0).getId()).isNotNull();
        assertThat(response.get(0).getSensorId()).isEqualTo(VALID_SENSOR_ID_2);
        assertThat(response.get(0).getSensorMac()).isEqualTo(VALID_SENSOR_MAC_2.toUpperCase());
        assertThat(response.get(0).getRowVersion()).isNotNull();
    }

    @Test
    void deleteBySensorId_shouldReturnOkStatus(){
        saveSensorMacListDirectly(getValidSensorMacDtoList());
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        webTestClient.delete()
                .uri("/api/sensormac/{VALID_SENSOR_ID_2}", VALID_SENSOR_ID_2)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorMacDtoOut.class);

        assertThat(sensorMacRepository.findAll().size()).isEqualTo(2);
        assertThat(sensorMacRepository.findBySensorId(VALID_SENSOR_ID_2)).isEmpty();
    }

    @Test
    void deleteSensorMac_nonExistentSensorId_shouldReturnNotFound() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        webTestClient.delete()
                .uri("/api/sensormac/nonexistent-sensor-id")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void ensureEndpointsRequireAdminRole_invalidUser_shouldReturnForbidden() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        List<SensorMacDtoIn> sensorMacDtoInList = getValidSensorMacDtoList();

        webTestClient.post()
                .uri("/api/sensormac")
                .header("Authorization", "Bearer " + respondentToken)
                .bodyValue(sensorMacDtoInList)
                .exchange()
                .expectStatus().isForbidden();
    }


    private List<SensorMacDtoIn> getValidSensorMacDtoList(){
        SensorMacDtoIn dto1 = new SensorMacDtoIn(VALID_SENSOR_ID_1, VALID_SENSOR_MAC_1);
        SensorMacDtoIn dto2 = new SensorMacDtoIn(VALID_SENSOR_ID_2, VALID_SENSOR_MAC_2);
        SensorMacDtoIn dto3 = new SensorMacDtoIn(VALID_SENSOR_ID_3, VALID_SENSOR_MAC_3);
        return List.of(dto1, dto2, dto3);
    }

    private void saveSensorMacListDirectly(List<SensorMacDtoIn> sensorMacDtoInList) {
        sensorMacDtoInList.forEach(dto -> {
            byte[] randomRowVersion = new byte[8];
            new java.util.Random().nextBytes(randomRowVersion);

            SensorMac sensorMac = new SensorMac(
                    UUID.randomUUID(),
                    dto.getSensorId(),
                    dto.getSensorMac().toUpperCase(),
                    randomRowVersion
            );

            sensorMacRepository.save(sensorMac);
        });
    }
}
