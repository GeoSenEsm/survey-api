package com.survey.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.surveyDtos.*;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class SurveyControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final SurveyRepository surveyRepository;
    private final AuthenticationManager authenticationManager;
    private final SurveyParticipationRepository surveyParticipationRepository;
    private static final String QUESTION_CONTENT = "What is your favorite color?";
    private static final int QUESTION_ORDER = 1;
    private static final String OPTION_CONTENT = "Red";
    private static final int OPTION_ORDER = 1;
    private static final String SURVEY_NAME = "Survey";
    private static final String SECTION_NAME = "Section1";
    private static final String adminPassword = "testAdminPassword";
    private String adminToken;

    @Autowired
    public SurveyControllerIntegrationTest(WebTestClient webTestClient, IdentityUserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider, SurveyRepository surveyRepository, AuthenticationManager authenticationManager, SurveyParticipationRepository surveyParticipationRepository) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.surveyRepository = surveyRepository;
        this.authenticationManager = authenticationManager;
        this.surveyParticipationRepository = surveyParticipationRepository;
    }
    @BeforeEach
    void SetUp(){
        surveyParticipationRepository.deleteAll();
        userRepository.deleteAll();
        surveyRepository.deleteAll();
        adminToken = authenticateAndGenerateTokenForAdmin();
    }
    @Test
    void createSurvey_ShouldBeOK() throws IOException {
        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        MultipartBodyBuilder multipartBodyBuilder = buildMultipartBodyFromDto(createSurveyDto);

        ResponseSurveyDto response = webTestClient.post()
                .uri("/api/surveys")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ResponseSurveyDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(SURVEY_NAME);
        assertThat(response.getSections().get(0).getQuestions().get(0).getContent()).isEqualTo(QUESTION_CONTENT);
        assertThat(response.getSections().get(0).getQuestions().get(0).getOptions().get(0).getLabel()).isEqualTo(OPTION_CONTENT);
    }

    @Test
    void getSurvey_ShouldReturnOk() throws JsonProcessingException {
        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/surveys")
                        .queryParam("surveyId", responseSurveyDto.getId().toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseSurveyDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getName()).isEqualTo(SURVEY_NAME);
    }

    @Test
    void updateSurvey_ShouldReturnOK() throws JsonProcessingException {
        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        MultipartBodyBuilder multipartBodyBuilder = buildMultipartBodyFromDto(createSurveyDto);
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        webTestClient.put()
                .uri("/api/surveys/" + responseSurveyDto.getId().toString())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void updateSurvey_ShouldReturnBadRequest_WhenSurveyAlreadyPublished() throws JsonProcessingException {
        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        MultipartBodyBuilder multipartBodyBuilder = buildMultipartBodyFromDto(createSurveyDto);
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        webTestClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/api/surveys/publish")
                        .queryParam("surveyId", responseSurveyDto.getId().toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isEqualTo(204);

        webTestClient.put()
                .uri("/api/surveys/" + responseSurveyDto.getId().toString())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void deleteSurvey_ShouldReturnOK() throws JsonProcessingException {
        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        webTestClient.delete()
                .uri("/api/surveys/" + responseSurveyDto.getId().toString())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void deleteSurvey_ShouldReturnNotFound_WhenNoSurveyWithThisId() {
        webTestClient.delete()
                .uri("/api/surveys/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class).value(errorMessage -> assertThat(errorMessage).contains("Survey not found"));
    }

    private MultipartBodyBuilder buildMultipartBodyFromDto(CreateSurveyDto createSurveyDto) throws JsonProcessingException {
        String jsonSurveyDto = new ObjectMapper().writeValueAsString(createSurveyDto);

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("json", jsonSurveyDto);
        return multipartBodyBuilder;
    }
    private ResponseSurveyDto saveSurveyAsAdmin(CreateSurveyDto createSurveyDto) throws JsonProcessingException {
        String jsonSurveyDto = new ObjectMapper().writeValueAsString(createSurveyDto);

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("json", jsonSurveyDto);

        return webTestClient.post()
                .uri("/api/surveys")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ResponseSurveyDto.class)
                .returnResult()
                .getResponseBody();
    }
    private CreateSurveyDto createValidSurveyDto(){
        CreateOptionDto createOptionDto = new CreateOptionDto();
        createOptionDto.setLabel(OPTION_CONTENT);
        createOptionDto.setOrder(OPTION_ORDER);
        createOptionDto.setImagePath(null);

        CreateQuestionDto createQuestionDto = new CreateQuestionDto();
        createQuestionDto.setQuestionType(QuestionType.single_choice.name());
        createQuestionDto.setOrder(QUESTION_ORDER);
        createQuestionDto.setContent(QUESTION_CONTENT);
        createQuestionDto.setOptions(List.of(createOptionDto));

        CreateSurveySectionDto createSurveySectionDto = new CreateSurveySectionDto();
        createSurveySectionDto.setName(SECTION_NAME);
        createSurveySectionDto.setOrder(1);
        createSurveySectionDto.setDisplayOnOneScreen(true);
        createSurveySectionDto.setVisibility("always");
        createSurveySectionDto.setQuestions(List.of(createQuestionDto));

        CreateSurveyDto createSurveyDto = new CreateSurveyDto();
        createSurveyDto.setName(SURVEY_NAME);
        createSurveyDto.setSections(List.of(createSurveySectionDto));
        return createSurveyDto;
    }
    private String authenticateAndGenerateTokenForAdmin() {
        IdentityUser admin = createUserWithRole("Admin", adminPassword);
        return authenticateAndGenerateToken(admin, adminPassword);
    }
    private IdentityUser createUserWithRole(String role, String password) {
        IdentityUser user = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole(role)
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode(password));

        return userRepository.saveAndFlush(user);
    }
    private String authenticateAndGenerateToken(IdentityUser user, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), password));
        return tokenProvider.generateToken(authentication);
    }
}
