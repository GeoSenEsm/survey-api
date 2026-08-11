package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.application.dtos.SensorDataValueDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorData;
import com.survey.domain.models.Survey;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.models.enums.SurveyState;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorDataRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

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
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private static final String RESPONDENT_PASSWORD = "testRespondentPassword";

    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final TestUtils testUtils;

    @Autowired
    public SensorDataControllerIntegrationTest(WebTestClient webTestClient,
                                               IdentityUserRepository userRepository,
                                               SensorDataRepository sensorDataRepository,
                                               SurveyRepository surveyRepository,
                                               SurveyParticipationRepository surveyParticipationRepository,
                                               TestUtils testUtils) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.sensorDataRepository = sensorDataRepository;
        this.surveyRepository = surveyRepository;
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.testUtils = testUtils;
    }

    @BeforeEach
    void setUp(){
        sensorDataRepository.deleteAll();
        surveyParticipationRepository.deleteAll();
        surveyRepository.deleteAll();
        userRepository.deleteAll();
        testUtils.getOrCreateXiaomiSensorType();
    }

    @Test
    void saveSensorData_ValidData_ShouldReturnCreatedStatus(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = createSensorDataDto(OffsetDateTime.now(UTC), "21.5", "60.4");

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
        assertThat(response.get(0).getSource()).isEqualTo("xiaomi");
        assertThat(response.get(0).getValues())
                .extracting(SensorDataValueDto::getParameterCode)
                .containsExactlyInAnyOrder("temperature", "humidity");
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent.getId());

    }

    @Test
    void saveSensorData_InvalidInputMissingValues_ShouldReturnBadRequest(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setSource("xiaomi");

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
        entryDto.setSource("xiaomi");
        entryDto.setValues(List.of(new SensorDataValueDto("temperature", "21.5")));

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveSensorData_InvalidUnknownParameter_ShouldReturnBadRequest() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        SensorDataDto entryDto = new SensorDataDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setSource("xiaomi");
        entryDto.setValues(List.of(new SensorDataValueDto("unknown", "100.0")));

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
    void getSensorData_ValidRange_ShouldReturnOkStatus(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        SensorDataDto entryDto = createSensorDataDto(OffsetDateTime.now(UTC), "21.5", "60.4");

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
        assertThat(response.get(0).getValues()).hasSize(2);
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent.getId());
        assertThat(response.get(0).getSurveyId()).isNull();
    }

    @Test
    void getSensorData_ReadingLinkedToSurveyParticipation_ShouldIncludeSurveyId() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        Survey survey = createSurvey();
        SurveyParticipation participation = createSurveyParticipation(survey, respondent);
        createLinkedSensorData(respondent, participation);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

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
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getSurveyId()).isEqualTo(survey.getId());
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

        SensorDataDto entryDto1 = createSensorDataDto(OffsetDateTime.now(UTC).minusDays(1), "21.5", "60.4");
        saveSensorData(respondent, entryDto1);

        SensorDataDto entryDto2 = createSensorDataDto(OffsetDateTime.now(UTC), "22.5", "60.4");
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

    private SensorDataDto createSensorDataDto(OffsetDateTime dateTime, String temperature, String humidity) {
        SensorDataDto dto = new SensorDataDto();
        dto.setDateTime(dateTime);
        dto.setSource("xiaomi");
        dto.setValues(List.of(
                new SensorDataValueDto("temperature", temperature),
                new SensorDataValueDto("humidity", humidity)));
        return dto;
    }

    private Survey createSurvey() {
        Survey survey = new Survey();
        survey.setName("Sensor data survey " + UUID.randomUUID());
        survey.setState(SurveyState.created);
        survey.setCreationDate(OffsetDateTime.now(UTC));
        return surveyRepository.save(survey);
    }

    private SurveyParticipation createSurveyParticipation(Survey survey, IdentityUser respondent) {
        SurveyParticipation participation = new SurveyParticipation();
        participation.setSurvey(survey);
        participation.setIdentityUser(respondent);
        participation.setDate(OffsetDateTime.now(UTC));
        return surveyParticipationRepository.save(participation);
    }

    private void createLinkedSensorData(IdentityUser respondent, SurveyParticipation participation) {
        SensorData sensorData = new SensorData();
        sensorData.setRespondent(respondent);
        sensorData.setDateTime(OffsetDateTime.now(UTC));
        sensorData.setSource("xiaomi");
        sensorData.setSurveyParticipation(participation);
        sensorDataRepository.save(sensorData);
    }

}
