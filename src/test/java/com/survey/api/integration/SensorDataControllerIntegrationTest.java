package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class SensorDataControllerIntegrationTest {
    private static final BigDecimal VALID_TEMPERATURE = new BigDecimal("21.5");
    private static final BigDecimal VALID_HUMIDITY = new BigDecimal("60.4");
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private static final String RESPONDENT_PASSWORD = "testRespondentPassword";

    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final SensorDataRepository sensorDataRepository;
    private final TestUtils testUtils;

    @Autowired
    public SensorDataControllerIntegrationTest(WebTestClient webTestClient,
                                               IdentityUserRepository userRepository,
                                               SensorDataRepository sensorDataRepository,
                                               TestUtils testUtils) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.sensorDataRepository = sensorDataRepository;
        this.testUtils = testUtils;
    }

    @BeforeEach
    void setUp(){
        sensorDataRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void saveSensorData_ValidData_ShouldReturnCreatedStatus(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(VALID_TEMPERATURE);
        entryDto.setHumidity(VALID_HUMIDITY);

        var response = webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(ResponseSensorDataDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getTemperature().compareTo(entryDto.getTemperature())).isEqualTo(0);
        assertThat(response.get(0).getHumidity().compareTo(entryDto.getHumidity())).isEqualTo(0);
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent.getId());

    }

    @Test
    void saveTemperatureData_InvalidInputMissingTemperatureField_ShouldReturnBadRequest(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setHumidity(VALID_HUMIDITY);

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveSensorData_InvalidInputMissingDateTimeField_ShouldReturnBadRequest() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setTemperature(VALID_TEMPERATURE);
        entryDto.setHumidity(VALID_HUMIDITY);

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveTemperatureData_InvalidTemperatureRange_ShouldReturnBadRequest() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(BigDecimal.valueOf(100.0));
        entryDto.setHumidity(VALID_HUMIDITY);

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getSensorData_InvalidRange_ShouldReturnBadRequest() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        OffsetDateTime from = OffsetDateTime.now(UTC).plusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).minusDays(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensordata")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveSensorData_DuplicateEntry_ShouldReturnConflict(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(VALID_TEMPERATURE);
        entryDto.setHumidity(VALID_HUMIDITY);

        saveSensorData(respondent, entryDto);

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void getSensorData_ValidRange_ShouldReturnOkStatus(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(VALID_TEMPERATURE);
        entryDto.setHumidity(VALID_HUMIDITY);

        saveSensorData(respondent, entryDto);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensordata")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseSensorDataDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
        assertThat(response.get(0).getTemperature().compareTo(entryDto.getTemperature())).isEqualTo(0);
        assertThat(response.get(0).getHumidity().compareTo(entryDto.getHumidity())).isEqualTo(0);
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent.getId());
    }

    @Test
    void getDateOfLastSensorDataForRespondent_NonExistentRespondent_ShouldReturnBadRequest(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        UUID nonExistentRespondentId = UUID.randomUUID();
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("api/sensordata/last")
                        .queryParam("respondentId", nonExistentRespondentId)
                        .build())
                .header("Authorization", "Bearer " + respondentToken)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getDateOfLastSensorDataForRespondent_ValidRespondent_ShouldReturnOkStatus() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto1 = new SensorDataDto();
        entryDto1.setDateTime(OffsetDateTime.now(UTC).minusDays(1));
        entryDto1.setTemperature(VALID_TEMPERATURE);
        entryDto1.setHumidity(VALID_HUMIDITY);
        saveSensorData(respondent, entryDto1);

        SensorDataDto entryDto2 = new SensorDataDto();
        entryDto2.setDateTime(OffsetDateTime.now(UTC));
        entryDto2.setTemperature(VALID_TEMPERATURE.add(BigDecimal.ONE));
        entryDto2.setHumidity(VALID_HUMIDITY);
        saveSensorData(respondent, entryDto2);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensordata/last")
                        .queryParam("respondentId", respondent.getId())
                        .build())
                .header("Authorization", "Bearer " + respondentToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LastSensorEntryDateDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();

        OffsetDateTime expected = entryDto2.getDateTime();
        OffsetDateTime actual = response.getDateTime();
        assertThat(actual).isBetween(expected.minusSeconds(1), expected.plusSeconds(1));
    }

    @Test
    void getLastSensorData_RespondentWithNoData_ShouldReturnNotFound() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensordata/last")
                        .queryParam("respondentId", respondent.getId())
                        .build())
                .header("Authorization", "Bearer " + respondentToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    private void saveSensorData(IdentityUser respondent, SensorDataDto entryDto){
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isCreated();
    }

}
