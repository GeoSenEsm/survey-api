package com.survey.api.integration;

import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.models.*;
import com.survey.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient()
public class RespondentDataControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RespondentDataRepository respondentDataRepository;
    private final InitialSurveyRepository initialSurveyRepository;
    private final AuthenticationManager authenticationManager;
    private static final String QUESTION_CONTENT = "What is your favorite color?";
    private static final int QUESTION_ORDER = 1;
    private static final String OPTION_CONTENT = "Red";
    private static final int OPTION_ORDER = 1;


    @Autowired
    public RespondentDataControllerIntegrationTest(WebTestClient webTestClient, IdentityUserRepository userRepository, PasswordEncoder passwordEncoder,
                                                   TokenProvider tokenProvider, RespondentDataRepository respondentDataRepository,
                                                   InitialSurveyRepository initialSurveyRepository, AuthenticationManager authenticationManager) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.respondentDataRepository = respondentDataRepository;
        this.initialSurveyRepository = initialSurveyRepository;
        this.authenticationManager = authenticationManager;
    }

    @Test
    void createRespondent_ShouldReturnCreatedResponse() {
        IdentityUser identityUser = new IdentityUser()
                .setRole("Respondent")
                .setUsername("username")
                .setPasswordHash(passwordEncoder.encode("password"));

        identityUser = userRepository.saveAndFlush(identityUser);

        InitialSurveyOption option = new InitialSurveyOption();
        option.setOrder(OPTION_ORDER);
        option.setContent(OPTION_CONTENT);

        InitialSurveyQuestion question = new InitialSurveyQuestion();
        question.setOrder(QUESTION_ORDER);
        question.setContent(QUESTION_CONTENT);
        question.setOptions(List.of(option));

        InitialSurvey initialSurvey = new InitialSurvey();
        initialSurvey.setQuestions(List.of(question));

        initialSurveyRepository.saveAndFlush(initialSurvey);

        CreateRespondentDataDto createDto = new CreateRespondentDataDto();
        createDto.setQuestionId(initialSurvey.getQuestions().get(0).getId());
        UUID optionId = initialSurvey.getQuestions().get(0).getOptions().get(0).getId();
        createDto.setOptionId(optionId);

        List<CreateRespondentDataDto> createDtoList = List.of(createDto);

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(identityUser.getUsername(),
                        "password"));

        String token = tokenProvider.generateToken(authentication);

        Map<String, Object> bodyRespondent = webTestClient.post().uri("/api/respondents")
                .header("Authorization", "Bearer " + token)
                .bodyValue(createDtoList)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                .returnResult()
                .getResponseBody();

        RespondentData respondentData = respondentDataRepository.findByIdentityUserId(identityUser.getId());

        assertThat(bodyRespondent).isNotNull();
        assertThat(bodyRespondent).containsEntry("id", respondentData.getId().toString());
        assertThat(bodyRespondent).containsEntry("username", identityUser.getUsername());
        assertThat(bodyRespondent).containsEntry(QUESTION_CONTENT, optionId.toString());
    }

    @Test
    void getFromUserContext_ShouldGiveNotFound_WhenTheRespondentDataWasNotCreatedYet(){
        IdentityUser identityUser = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole("Respondent")
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode("password"));

        identityUser = userRepository.saveAndFlush(identityUser);

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(identityUser.getUsername(),
                        "password"));

        String token = tokenProvider.generateToken(authentication);

        webTestClient.get().uri("/api/respondents")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getAllForAdminShouldBeOk(){
        IdentityUser identityUser = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole("ADMIN")
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode("pswd"));

        identityUser = userRepository.saveAndFlush(identityUser);

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(identityUser.getUsername(),
                        "pswd"));

        String token = tokenProvider.generateToken(authentication);

        webTestClient.get().uri("/api/respondents/all")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();
    }
}