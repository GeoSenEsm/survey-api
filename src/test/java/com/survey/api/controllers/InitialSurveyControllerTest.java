package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyOptionDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyOptionResponseDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.InitialSurveyService;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class InitialSurveyControllerTest {

    @InjectMocks
    private InitialSurveyController initialSurveyController;

    @Mock
    private InitialSurveyService initialSurveyService;
    @Mock
    private IdentityUserRepository identityUserRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private ClaimsPrincipalService claimsPrincipalService;

    private WebTestClient webTestClient;
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private String adminToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(initialSurveyController).build();

        IdentityUser admin = createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        adminToken = "Bearer " + authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
    }

    @Test
    void createInitialSurvey_ShouldReturnCreatedResponse() {
        CreateInitialSurveyQuestionDto questionDto = createQuestionDto();
        InitialSurveyQuestionResponseDto responseDto = createQuestionResponseDto();

        when(initialSurveyService.createInitialSurvey(any()))
                .thenReturn(Collections.singletonList(responseDto));

        webTestClient.post()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Collections.singletonList(questionDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .hasSize(1)
                .consumeWith(response -> {
                    List<InitialSurveyQuestionResponseDto> body = response.getResponseBody();
                    assert body != null;
                    assert body.get(0).getContent().equals("Question1");
                    assert body.get(0).getOrder().equals(1);
                    assert body.get(0).getOptions().get(0).getContent().equals("Option1");
                    assert body.get(0).getOptions().get(0).getOrder().equals(1);
                });

        verify(initialSurveyService, times(1)).createInitialSurvey(anyList());
    }

    @Test
    void getInitialSurvey_ShouldReturnOkResponse() {
        InitialSurveyQuestionResponseDto responseDto = createQuestionResponseDto();

        when(initialSurveyService.getInitialSurvey())
                .thenReturn(Collections.singletonList(responseDto));

        webTestClient.get()
                .uri("/api/initialsurvey")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .hasSize(1)
                .consumeWith(response -> {
                    List<InitialSurveyQuestionResponseDto> body = response.getResponseBody();
                    assert body != null;
                    assert body.get(0).getContent().equals("Question1");
                    assert body.get(0).getOrder().equals(1);
                    assert body.get(0).getOptions().get(0).getContent().equals("Option1");
                    assert body.get(0).getOptions().get(0).getOrder().equals(1);
                });

        verify(initialSurveyService, times(1)).getInitialSurvey();
    }

    private InitialSurveyQuestionResponseDto createQuestionResponseDto() {
        InitialSurveyQuestionResponseDto responseDto = new InitialSurveyQuestionResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setContent("Question1");
        responseDto.setOrder(1);
        responseDto.setOptions(List.of(createOptionResponseDto()));
        return responseDto;
    }

    private InitialSurveyOptionResponseDto createOptionResponseDto() {
        InitialSurveyOptionResponseDto optionResponseDto = new InitialSurveyOptionResponseDto();
        optionResponseDto.setId(UUID.randomUUID());
        optionResponseDto.setOrder(1);
        optionResponseDto.setContent("Option1");
        return optionResponseDto;
    }

    private CreateInitialSurveyQuestionDto createQuestionDto() {
        CreateInitialSurveyOptionDto optionDto = new CreateInitialSurveyOptionDto();
        optionDto.setContent("Option1");
        optionDto.setOrder(1);

        CreateInitialSurveyQuestionDto questionDto = new CreateInitialSurveyQuestionDto();
        questionDto.setContent("Question1");
        questionDto.setOrder(1);
        questionDto.setOptions(List.of(optionDto));
        return questionDto;
    }

    private IdentityUser createUserWithRole(String role, String password) {
        IdentityUser user = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole(role)
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(new BCryptPasswordEncoder().encode(password));

        when(identityUserRepository.saveAndFlush(any(IdentityUser.class))).thenReturn(user);
        return user;
    }

    private String authenticateAndGenerateToken(IdentityUser user, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), password);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        String token = UUID.randomUUID().toString();
        when(tokenProvider.generateToken(authentication)).thenReturn(token);
        return token;
    }


}
