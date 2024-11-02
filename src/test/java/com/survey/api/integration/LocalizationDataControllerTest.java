package com.survey.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.LocalizationDataRepository;
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
public class LocalizationDataControllerTest {
    private static final BigDecimal VALID_LATITUDE = new BigDecimal("52.237049");
    private static final BigDecimal VALID_LONGITUDE = new BigDecimal("21.017532");


    private final WebTestClient webTestClient;
    private final ObjectMapper objectMapper;
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final LocalizationDataRepository localizationDataRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public LocalizationDataControllerTest(WebTestClient webTestClient, ObjectMapper objectMapper, IdentityUserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider, LocalizationDataRepository localizationDataRepository, AuthenticationManager authenticationManager) {
        this.webTestClient = webTestClient;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.localizationDataRepository = localizationDataRepository;
        this.authenticationManager = authenticationManager;
    }

    @BeforeEach
    void setUp() {
        localizationDataRepository.deleteAll();
        userRepository.deleteAll();
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
    void getLocalizationData_ValidRange_ShouldReturnOkStatus() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        LocalizationDataDto localizationDataDto = new LocalizationDataDto();
        localizationDataDto.setLatitude(VALID_LATITUDE);
        localizationDataDto.setLongitude(VALID_LONGITUDE);
        localizationDataDto.setDateTime(OffsetDateTime.now(UTC));

        saveLocalizationData(user, localizationDataDto);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/localization")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseLocalizationDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
        assertThat(response.get(0).getLatitude()).isEqualByComparingTo(localizationDataDto.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualByComparingTo(localizationDataDto.getLongitude());
        assertThat(response.get(0).getRespondentId()).isEqualTo(user.getId());
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
}
