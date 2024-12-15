package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.RespondentDataService;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.enums.RespondentFilterOption;
import com.survey.domain.repository.IdentityUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class RespondentDataControllerTest {

    @InjectMocks
    private RespondentDataController respondentDataController;

    @Mock
    private RespondentDataService respondentDataService;
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
        webTestClient = WebTestClient.bindToController(respondentDataController).build();

        IdentityUser admin = createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        adminToken = "Bearer " + authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
    }

    @Test
    void createRespondent_ShouldReturnCreatedResponse() throws Exception {
        CreateRespondentDataDto dto = new CreateRespondentDataDto();

        Map<String, Object> responseMap = createResponseMap();
        when(respondentDataService.createRespondent(anyList()))
                .thenReturn(responseMap);

        webTestClient.post()
                .uri("/api/respondents")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(Collections.singletonList(dto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                .consumeWith(response -> {
                    Map<String, Object> body = response.getResponseBody();
                    assert body != null;
                    assert body.get("username").equals("User1");
                    assert body.get("id").equals(1);
                });

        verify(respondentDataService, times(1)).createRespondent(anyList());
    }
    @Test
    void getAll_ShouldReturnOkResponse() {
        Map<String, Object> responseItem = createResponseMap();
        when(respondentDataService.getAll(any(RespondentFilterOption.class), any(Integer.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(responseItem));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/respondents/all")
                        .queryParam("filterOption", RespondentFilterOption.skipped_surveys)
                        .queryParam("amount", 10)
                        .queryParam("from", "2024-01-01T00:00:00Z")
                        .queryParam("to", "2024-12-31T23:59:59Z")
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<Map<String, Object>>() {})
                .hasSize(1)
                .consumeWith(response -> {
                    List<Map<String, Object>> body = response.getResponseBody();
                    assert body != null;
                    assert body.get(0).get("username").equals("User1");
                    assert body.get(0).get("id").equals(1);
                });

        verify(respondentDataService, times(1)).getAll(any(RespondentFilterOption.class), any(Integer.class), any(OffsetDateTime.class), any(OffsetDateTime.class));
    }
    @Test
    void getFromUserContext_ShouldReturnOkResponse() {
        Map<String, Object> responseItem = createResponseMap();
        when(respondentDataService.getFromUserContext())
                .thenReturn(responseItem);

        webTestClient.get()
                .uri("/api/respondents")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                .consumeWith(response -> {
                    Map<String, Object> body = response.getResponseBody();
                    assert body != null;
                    assert body.get("username").equals("User1");
                    assert body.get("id").equals(1);
                });

        verify(respondentDataService, times(1)).getFromUserContext();
    }

    private Map<String, Object> createResponseMap() {
        return Map.of("id", 1, "username", "User1");
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
