package com.survey.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.ResponseTemperatureDataEntryDto;
import com.survey.application.dtos.TemperatureDataEntryDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.TemperatureDataRepository;
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
public class TemperatureDataControllerTest {
    private final WebTestClient webTestClient;
    private final ObjectMapper objectMapper;
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final TemperatureDataRepository temperatureDataRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public TemperatureDataControllerTest(WebTestClient webTestClient, ObjectMapper objectMapper, IdentityUserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider, TemperatureDataRepository temperatureDataRepository, AuthenticationManager authenticationManager) {
        this.webTestClient = webTestClient;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.temperatureDataRepository = temperatureDataRepository;
        this.authenticationManager = authenticationManager;
    }

    @BeforeEach
    void setUp(){
        temperatureDataRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void saveTemperatureData_ValidData_ShouldReturnCreatedStatus(){
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        TemperatureDataEntryDto entryDto = new TemperatureDataEntryDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(BigDecimal.valueOf(21.5));

        var response = webTestClient.post()
                .uri("/api/temperaturedata")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(ResponseTemperatureDataEntryDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getTemperature().compareTo(entryDto.getTemperature())).isEqualTo(0);
        assertThat(response.get(0).getRespondentId()).isEqualTo(user.getId());

    }

    @Test
    void saveTemperatureData_InvalidInputMissingTemperatureField_ShouldReturnBadRequest(){
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        TemperatureDataEntryDto entryDto = new TemperatureDataEntryDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));

        webTestClient.post()
                .uri("/api/temperaturedata")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveTemperatureData_InvalidInputMissingDateTimeField_ShouldReturnBadRequest() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        TemperatureDataEntryDto entryDto = new TemperatureDataEntryDto();
        entryDto.setTemperature(BigDecimal.valueOf(21.5));

        webTestClient.post()
                .uri("/api/temperaturedata")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveTemperatureData_InvalidTemperatureRange_ShouldReturnBadRequest() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        TemperatureDataEntryDto entryDto = new TemperatureDataEntryDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(BigDecimal.valueOf(100.0));

        webTestClient.post()
                .uri("/api/temperaturedata")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getTemperatureData_InvalidRange_ShouldReturnBadRequest() {
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        OffsetDateTime from = OffsetDateTime.now(UTC).plusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).minusDays(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/temperaturedata")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveTemperatureData_DuplicateEntry_ShouldReturnConflict(){
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        TemperatureDataEntryDto entryDto = new TemperatureDataEntryDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(BigDecimal.valueOf(21.5));

        saveTemperatureData(user, entryDto);

        webTestClient.post()
                .uri("/api/temperaturedata")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void getTemperatureData_ValidRange_ShouldReturnOkStatus(){
        IdentityUser user = createUserWithRole("Respondent");
        String token = authenticateAndGenerateToken(user);

        OffsetDateTime from = OffsetDateTime.now(UTC).minusDays(1);
        OffsetDateTime to = OffsetDateTime.now(UTC).plusDays(1);

        TemperatureDataEntryDto entryDto = new TemperatureDataEntryDto();
        entryDto.setDateTime(OffsetDateTime.now(UTC));
        entryDto.setTemperature(BigDecimal.valueOf(21.50));

        saveTemperatureData(user, entryDto);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/temperaturedata")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build())
                .header("Authorization", "Bearer" + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseTemperatureDataEntryDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
        assertThat(response.get(0).getTemperature().compareTo(entryDto.getTemperature())).isEqualTo(0);
        assertThat(response.get(0).getRespondentId()).isEqualTo(user.getId());
    }


    private IdentityUser createUserWithRole(String role){
        IdentityUser user = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole(role)
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode("pswd"));

        return userRepository.saveAndFlush(user);
    }

    private String authenticateAndGenerateToken(IdentityUser user){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), "pswd"));
        return tokenProvider.generateToken(authentication);
    }

    private void saveTemperatureData(IdentityUser user, TemperatureDataEntryDto entryDto){
        String token = authenticateAndGenerateToken(user);

        webTestClient.post()
                .uri("/api/temperaturedata")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(entryDto))
                .exchange()
                .expectStatus().isCreated();
    }
}
