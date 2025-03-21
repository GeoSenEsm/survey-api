package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.LocalizationDataRepository;
import com.survey.domain.repository.ResearchAreaRepository;
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
public class LocalizationDataControllerIntegrationTest {
    private static final BigDecimal VALID_LATITUDE = new BigDecimal("52.237049");
    private static final BigDecimal VALID_LONGITUDE = new BigDecimal("21.017532");
    private static final BigDecimal VALID_ACCURACY = new BigDecimal("7.55");
    private static final BigDecimal INVALID_LATITUDE = new BigDecimal("60.237049");
    private static final BigDecimal INVALID_LONGITUDE = new BigDecimal("60.017532");
    private static final BigDecimal NEGATIVE_ACCURACY = new BigDecimal("-1.0");
    private static final BigDecimal OVERFLOW_ACCURACY = new BigDecimal("1234567.89");
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private static final String RESPONDENT_PASSWORD = "testRespondentPassword";

    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final LocalizationDataRepository localizationDataRepository;
    private final ResearchAreaRepository researchAreaRepository;
    private final TestUtils testUtils;

    @Autowired
    public LocalizationDataControllerIntegrationTest(WebTestClient webTestClient, IdentityUserRepository userRepository, LocalizationDataRepository localizationDataRepository, ResearchAreaRepository researchAreaRepository, TestUtils testUtils) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.localizationDataRepository = localizationDataRepository;
        this.researchAreaRepository = researchAreaRepository;
        this.testUtils = testUtils;
    }

    @BeforeEach
    void setUp() {
        localizationDataRepository.deleteAll();
        userRepository.deleteAll();
        researchAreaRepository.deleteAll();
    }

    @Test
    void saveLocalizationData_ValidData_ShouldReturnCreatedStatus() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));
        localizationDataDto.setAccuracyMeters(VALID_ACCURACY);

        var response = webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto.getLongitude());
        assertThat(response.get(0).getAccuracyMeters()).isEqualByComparingTo(localizationDataDto.getAccuracyMeters());
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent.getId());
    }

    @Test
    void saveLocalizationData_InvalidInputMissingLatitude_ShouldReturnBadRequest() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveLocalizationData_InvalidInputMissingLongitude_ShouldReturnBadRequest() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveLocalizationData_InvalidSurveyParticipationId_ShouldReturnBadRequest(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setSurveyParticipationId(UUID.randomUUID());
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void saveLocalizationData_ValidSurveyParticipationId_ShouldReturnCreatedStatus(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();

        localizationDataDto.setSurveyParticipationId(UUID.randomUUID());
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));
        localizationDataDto.setAccuracyMeters(VALID_ACCURACY);

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void saveLocalizationData_NegativeAccuracyMeters_ShouldReturnBadRequest(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();

        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));
        localizationDataDto.setAccuracyMeters(NEGATIVE_ACCURACY);

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveLocalizationData_OverflowAccuracyMeters_ShouldReturnBadRequest(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();

        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));
        localizationDataDto.setAccuracyMeters(OVERFLOW_ACCURACY);

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void getLocalizationData_InvalidRange_ShouldReturnBadRequest() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        OffsetDateTime from = OffsetDateTime.now(UTC).plusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).minusDays(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getLocalizationData_WithRespondentId_ShouldReturnFilteredData() {
        IdentityUser respondent1 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        IdentityUser respondent2 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(respondent1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(respondent2, localizationDataDto2);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .queryParam("respondentId", respondent1.getId().toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto1.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto1.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent1.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto1.getDateTime());
    }

    @Test
    void getLocalizationData_WithoutRespondentId_ShouldReturnAllData() {
        IdentityUser respondent1 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        IdentityUser respondent2 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(respondent1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC).plusHours(1));
        saveLocalizationData(respondent2, localizationDataDto2);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto1.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto1.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent1.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto1.getDateTime());

        assertThat(response.get(1).getLatitude()).isEqualByComparingTo(localizationDataDto2.getLatitude());
        assertThat(response.get(1).getLongitude()).isEqualByComparingTo(localizationDataDto2.getLongitude());
        assertThat(response.get(1).getRespondentId()).isEqualTo(respondent2.getId());
        assertThat(response.get(1).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto2.getDateTime());
    }

    @Test
    void getLocalizationData_WithoutParams_ShouldReturnAllData() {
        IdentityUser respondent1 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        IdentityUser respondent2 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(respondent1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC).plusHours(1));
        saveLocalizationData(respondent2, localizationDataDto2);

        var response = webTestClient.get()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto1.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto1.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent1.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto1.getDateTime());

        assertThat(response.get(1).getLatitude()).isEqualByComparingTo(localizationDataDto2.getLatitude());
        assertThat(response.get(1).getLongitude()).isEqualByComparingTo(localizationDataDto2.getLongitude());
        assertThat(response.get(1).getRespondentId()).isEqualTo(respondent2.getId());
        assertThat(response.get(1).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto2.getDateTime());
    }

    @Test
    void getLocalizationData_WithOutsideResearchAreaParam_ShouldReturnValidData() {
        IdentityUser respondent1 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        IdentityUser respondent2 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(respondent1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(INVALID_LATITUDE);
        localizationDataDto2.setLongitude(INVALID_LONGITUDE);
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC).plusHours(1));

        saveLocalizationData(respondent2, localizationDataDto2);
        saveResearchArea();

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("outsideResearchArea", true)
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto2.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto2.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(respondent2.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto2.getDateTime());
    }

    private void saveLocalizationData(IdentityUser respondent, LocalizationDataDto localizationDataDto) {
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isCreated();
    }

    private void saveResearchArea() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        ResearchAreaDto researchAreaDto1 = new ResearchAreaDto();
        researchAreaDto1.setLatitude(new BigDecimal(55));
        researchAreaDto1.setLongitude(new BigDecimal(-25));

        ResearchAreaDto researchAreaDto2 = new ResearchAreaDto();
        researchAreaDto2.setLatitude(new BigDecimal(-55));
        researchAreaDto2.setLongitude(new BigDecimal(-25));

        ResearchAreaDto researchAreaDto3 = new ResearchAreaDto();
        researchAreaDto3.setLatitude(new BigDecimal(-55));
        researchAreaDto3.setLongitude(new BigDecimal(25));

        ResearchAreaDto researchAreaDto4 = new ResearchAreaDto();
        researchAreaDto4.setLatitude(new BigDecimal(55));
        researchAreaDto4.setLongitude(new BigDecimal(25));

        webTestClient.post()
                .uri("/api/researcharea")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(researchAreaDto1, researchAreaDto2, researchAreaDto3, researchAreaDto4))
                .exchange()
                .expectStatus().isCreated();
    }
}
