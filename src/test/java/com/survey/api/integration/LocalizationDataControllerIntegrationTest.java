package com.survey.api.integration;

import com.survey.api.security.TokenProvider;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class LocalizationDataControllerIntegrationTest {
    private static final BigDecimal VALID_LATITUDE = new BigDecimal("52.237049");
    private static final BigDecimal VALID_LONGITUDE = new BigDecimal("21.017532");
    private static final BigDecimal INVALID_LATITUDE = new BigDecimal("60.237049");
    private static final BigDecimal INVALID_LONGITUDE = new BigDecimal("60.017532");
    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final LocalizationDataRepository localizationDataRepository;
    private final AuthenticationManager authenticationManager;
    private final ResearchAreaRepository researchAreaRepository;

    @Autowired
    public LocalizationDataControllerIntegrationTest(WebTestClient webTestClient, IdentityUserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider, LocalizationDataRepository localizationDataRepository, AuthenticationManager authenticationManager, ResearchAreaRepository researchAreaRepository) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.localizationDataRepository = localizationDataRepository;
        this.authenticationManager = authenticationManager;
        this.researchAreaRepository = researchAreaRepository;
    }

    @BeforeEach
    void setUp() {
        localizationDataRepository.deleteAll();
        userRepository.deleteAll();
        researchAreaRepository.deleteAll();
    }

    @Test
    void saveLocalizationData_ValidData_ShouldReturnCreatedStatus() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        var response = webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + token)
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
        assertThat(response.get(0).getRespondentId()).isEqualTo(user.getId());
    }

    @Test
    void saveLocalizationData_InvalidInputMissingLatitude_ShouldReturnBadRequest() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveLocalizationData_InvalidInputMissingLongitude_ShouldReturnBadRequest() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveLocalizationData_InvalidSurveyParticipationId_ShouldReturnBadRequest(){
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setSurveyParticipationId(UUID.randomUUID());
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void saveLocalizationData_ValidSurveyParticipationId_ShouldReturnCreatedStatus(){
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();

        localizationDataDto.setSurveyParticipationId(UUID.randomUUID());
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void getLocalizationData_InvalidRange_ShouldReturnBadRequest() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        OffsetDateTime from = OffsetDateTime.now(UTC).plusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).minusDays(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getLocalizationData_WithRespondentId_ShouldReturnFilteredData() {
        IdentityUser user1 = createUserWithRole("Respondent");
        IdentityUser user2 = createUserWithRole("Respondent");

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(user1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(user2, localizationDataDto2);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .queryParam("respondentId", user1.getId().toString())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto1.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto1.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(user1.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto1.getDateTime());
    }

    @Test
    void getLocalizationData_WithoutRespondentId_ShouldReturnAllData() {
        IdentityUser user1 = createUserWithRole("Respondent");
        IdentityUser user2 = createUserWithRole("Respondent");

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(user1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC).plusHours(1));
        saveLocalizationData(user2, localizationDataDto2);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto1.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto1.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(user1.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto1.getDateTime());

        assertThat(response.get(1).getLatitude()).isEqualByComparingTo(localizationDataDto2.getLatitude());
        assertThat(response.get(1).getLongitude()).isEqualByComparingTo(localizationDataDto2.getLongitude());
        assertThat(response.get(1).getRespondentId()).isEqualTo(user2.getId());
        assertThat(response.get(1).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto2.getDateTime());
    }

    @Test
    void getLocalizationData_WithoutParams_ShouldReturnAllData() {
        IdentityUser user1 = createUserWithRole("Respondent");
        IdentityUser user2 = createUserWithRole("Respondent");

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(user1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC).plusHours(1));
        saveLocalizationData(user2, localizationDataDto2);

        var response = webTestClient.get()
                .uri("/api/localization")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto1.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto1.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(user1.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto1.getDateTime());

        assertThat(response.get(1).getLatitude()).isEqualByComparingTo(localizationDataDto2.getLatitude());
        assertThat(response.get(1).getLongitude()).isEqualByComparingTo(localizationDataDto2.getLongitude());
        assertThat(response.get(1).getRespondentId()).isEqualTo(user2.getId());
        assertThat(response.get(1).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto2.getDateTime());
    }

    @Test
    void getLocalizationData_WithOutsideResearchAreaParam_ShouldReturnValidData() {
        IdentityUser user1 = createUserWithRole("Respondent");
        IdentityUser user2 = createUserWithRole("Respondent");

        LocalizationDataDto localizationDataDto1 = new LocalizationDataDto();
        localizationDataDto1.setLatitude(VALID_LATITUDE);
        localizationDataDto1.setLongitude(VALID_LONGITUDE);
        localizationDataDto1.setDateTime(OffsetDateTime.now(UTC));
        saveLocalizationData(user1, localizationDataDto1);

        LocalizationDataDto localizationDataDto2 = new LocalizationDataDto();
        localizationDataDto2.setLatitude(INVALID_LATITUDE);
        localizationDataDto2.setLongitude(INVALID_LONGITUDE);
        localizationDataDto2.setDateTime(OffsetDateTime.now(UTC).plusHours(1));

        saveLocalizationData(user2, localizationDataDto2);
        saveResearchArea();

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("outsideResearchArea", true)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto2.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto2.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(user2.getId());
        assertThat(response.get(0).getDateTime()).isEqualToIgnoringSeconds(localizationDataDto2.getDateTime());
    }

    private IdentityUser createUserWithRole(String role) {
        IdentityUser user = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole(role)
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode("pswd"));

        return userRepository.saveAndFlush(user);
    }

    private String authenticateAndGenerateToken(IdentityUser user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), "pswd"));
        return tokenProvider.generateToken(authentication);
    }

    private void saveLocalizationData(IdentityUser user, LocalizationDataDto localizationDataDto) {
        String token = authenticateAndGenerateToken(user);

        webTestClient.post()
                .uri("/api/localization")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(localizationDataDto))
                .exchange()
                .expectStatus().isCreated();
    }
    private void saveResearchArea() {
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
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(researchAreaDto1, researchAreaDto2, researchAreaDto3, researchAreaDto4))
                .exchange()
                .expectStatus().isCreated();
    }
}
