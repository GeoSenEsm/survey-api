package com.survey.api.integration;

import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyOptionDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.InitialSurveyRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class InitialSurveyControllerIntegrationTest {
    private static final String adminPassword = "testAdminPassword";
    private static final String respondentPassword = "testRespondentPassword";
    private static final String QUESTION_1 = "Gender";
    private static final String OPTION_1_1 = "male";
    private static final String OPTION_1_2 = "female";
    private static final String QUESTION_2 = "Student";
    private static final String OPTION_2_1 = "yes";
    private static final String OPTION_2_2 = "no";
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final InitialSurveyRepository initialSurveyRepository;
    private final WebTestClient webTestClient;

    @Autowired
    public InitialSurveyControllerIntegrationTest(IdentityUserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenProvider tokenProvider, InitialSurveyRepository initialSurveyRepository, WebTestClient webTestClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.initialSurveyRepository = initialSurveyRepository;
        this.webTestClient = webTestClient;
    }

    @BeforeEach
    void setUp(){
        initialSurveyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createInitialSurveyAsAdmin_ShouldReturnCreatedStatus_WhenInitialSurveyIsNotPublished(){
        IdentityUser admin = createUserWithRole("Admin", adminPassword);
        String adminToken = authenticateAndGenerateToken(admin, adminPassword);

        List<CreateInitialSurveyQuestionDto> initialSurveyCreateDto = generateValidInitialSurveyCreateDto();

        var response = webTestClient.post()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(initialSurveyCreateDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        assertThat(response.get(0).getContent()).isEqualTo(QUESTION_1);
        assertThat(response.get(0).getOptions().get(0).getContent()).isEqualTo(OPTION_1_1);
        assertThat(response.get(0).getOptions().get(1).getContent()).isEqualTo(OPTION_1_2);

        assertThat(response.get(1).getContent()).isEqualTo(QUESTION_2);
        assertThat(response.get(1).getOptions().get(0).getContent()).isEqualTo(OPTION_2_1);
        assertThat(response.get(1).getOptions().get(1).getContent()).isEqualTo(OPTION_2_2);

    }

    @Test
    void createInitialSurveyAsAdmin_ShouldReturnBadRequest_WhenInitialSurveyIsAlreadyPublished(){
        IdentityUser admin = createUserWithRole("Admin", adminPassword);
        String adminToken = authenticateAndGenerateToken(admin, adminPassword);

        List<CreateInitialSurveyQuestionDto> initialSurveyCreateDto = generateValidInitialSurveyCreateDto();
        saveInitialSurveyAsAdmin(initialSurveyCreateDto);

        webTestClient.patch()
                .uri("/api/initialsurvey/publish")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isEqualTo(204);

        webTestClient.post()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(initialSurveyCreateDto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void publishInitialSurveyAsAdmin_ShouldReturnBadRequest_WhenInitialSurveyIsAlreadyPublished(){
        IdentityUser admin = createUserWithRole("Admin", adminPassword);
        String adminToken = authenticateAndGenerateToken(admin, adminPassword);

        List<CreateInitialSurveyQuestionDto> initialSurveyCreateDto = generateValidInitialSurveyCreateDto();
        saveInitialSurveyAsAdmin(initialSurveyCreateDto);

        webTestClient.patch()
                .uri("/api/initialsurvey/publish")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isEqualTo(204);

        webTestClient.patch()
                .uri("/api/initialsurvey/publish")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getInitialSurveyAsRespondent_ShouldReturnNotFound_WhenInitialSurveyExistsButIsNotPublished(){
        IdentityUser respondent = createUserWithRole("Respondent", respondentPassword);
        String respondentToken = authenticateAndGenerateToken(respondent, respondentPassword);

        List<CreateInitialSurveyQuestionDto> initialSurveyCreateDto = generateValidInitialSurveyCreateDto();
        saveInitialSurveyAsAdmin(initialSurveyCreateDto);

        webTestClient.get()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + respondentToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getInitialSurveyAsRespondent_ShouldReturnOkStatus_WhenInitialSurveyIsPublished(){
        List<CreateInitialSurveyQuestionDto> initialSurveyCreateDto = generateValidInitialSurveyCreateDto();
        saveInitialSurveyAsAdmin(initialSurveyCreateDto);

        IdentityUser admin = createUserWithRole("Admin", adminPassword);
        String adminToken = authenticateAndGenerateToken(admin, adminPassword);

        webTestClient.patch()
                .uri("/api/initialsurvey/publish")
                .header("Authorization", "Bearer " + adminToken)
                .exchange();

        IdentityUser respondent = createUserWithRole("Respondent", respondentPassword);
        String respondentToken = authenticateAndGenerateToken(respondent, respondentPassword);

        var response = webTestClient.get()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + respondentToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        assertThat(response.get(0).getContent()).isEqualTo(QUESTION_1);
        assertThat(response.get(0).getOptions().get(0).getContent()).isEqualTo(OPTION_1_1);
        assertThat(response.get(0).getOptions().get(1).getContent()).isEqualTo(OPTION_1_2);

        assertThat(response.get(1).getContent()).isEqualTo(QUESTION_2);
        assertThat(response.get(1).getOptions().get(0).getContent()).isEqualTo(OPTION_2_1);
        assertThat(response.get(1).getOptions().get(1).getContent()).isEqualTo(OPTION_2_2);

    }

    @Test
    void getInitialSurveyAsAdmin_ShouldReturnOkStatus_WhenInitialSurveyIsPublished(){
        List<CreateInitialSurveyQuestionDto> initialSurveyCreateDto = generateValidInitialSurveyCreateDto();
        saveInitialSurveyAsAdmin(initialSurveyCreateDto);

        IdentityUser admin = createUserWithRole("Admin", adminPassword);
        String adminToken = authenticateAndGenerateToken(admin, adminPassword);

        webTestClient.patch()
                .uri("/api/initialsurvey/publish")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isEqualTo(204);

        var response = webTestClient.get()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        assertThat(response.get(0).getContent()).isEqualTo(QUESTION_1);
        assertThat(response.get(0).getOptions().get(0).getContent()).isEqualTo(OPTION_1_1);
        assertThat(response.get(0).getOptions().get(1).getContent()).isEqualTo(OPTION_1_2);

        assertThat(response.get(1).getContent()).isEqualTo(QUESTION_2);
        assertThat(response.get(1).getOptions().get(0).getContent()).isEqualTo(OPTION_2_1);
        assertThat(response.get(1).getOptions().get(1).getContent()).isEqualTo(OPTION_2_2);
    }



    private void saveInitialSurveyAsAdmin(List<CreateInitialSurveyQuestionDto> initialSurveyCreateDto){
        IdentityUser admin = createUserWithRole("Admin", adminPassword);
        String adminToken = authenticateAndGenerateToken(admin, adminPassword);

        webTestClient.post()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(initialSurveyCreateDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .returnResult().getResponseBody();
    }

    private List<CreateInitialSurveyQuestionDto> generateValidInitialSurveyCreateDto(){
        CreateInitialSurveyOptionDto optionDto1_1 = new CreateInitialSurveyOptionDto(0, OPTION_1_1);
        CreateInitialSurveyOptionDto optionDto1_2 = new CreateInitialSurveyOptionDto(1, OPTION_1_2);
        CreateInitialSurveyQuestionDto questionDto1 = new CreateInitialSurveyQuestionDto(0, QUESTION_1, List.of(optionDto1_1, optionDto1_2));

        CreateInitialSurveyOptionDto optionDto2_1 = new CreateInitialSurveyOptionDto(0, OPTION_2_1);
        CreateInitialSurveyOptionDto optionDto2_2 = new CreateInitialSurveyOptionDto(1, OPTION_2_2);
        CreateInitialSurveyQuestionDto questionDto2 = new CreateInitialSurveyQuestionDto(1, QUESTION_2, List.of(optionDto2_1, optionDto2_2));

        return List.of(questionDto1, questionDto2);
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
