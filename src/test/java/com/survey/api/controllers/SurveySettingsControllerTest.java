package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.SurveySettingsDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SurveySettingsService;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SurveySettingsControllerTest {
    @InjectMocks
    private SurveySettingsController surveySettingsController;
    @Mock
    private SurveySettingsService surveySettingsService;
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
        webTestClient = WebTestClient.bindToController(surveySettingsController).build();

        IdentityUser admin = createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        adminToken = "Bearer " + authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
    }

    @Test
    void uploadLogo_ShouldReturnUpdatedSettings() throws Exception {
        SurveySettingsDto responseDto = new SurveySettingsDto(true, ",", ".", "/uploads/survey_settings/logo.png");
        MultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", new byte[]{1, 2, 3});
        when(surveySettingsService.uploadLogo(file)).thenReturn(responseDto);

        ResponseEntity<SurveySettingsDto> response = surveySettingsController.uploadLogo(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().logoPath()).isEqualTo("/uploads/survey_settings/logo.png");
        verify(claimsPrincipalService).ensureRole(Role.ADMIN.getRoleName());
        verify(surveySettingsService).uploadLogo(file);
    }

    @Test
    void deleteLogo_ShouldReturnUpdatedSettings() {
        SurveySettingsDto responseDto = new SurveySettingsDto(true, ",", ".", null);
        when(surveySettingsService.deleteLogo()).thenReturn(responseDto);

        webTestClient.delete()
                .uri("/api/surveysettings/logo")
                .header("Authorization", adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SurveySettingsDto.class)
                .value(result -> assertThat(result.logoPath()).isNull());
        verify(claimsPrincipalService).ensureRole(Role.ADMIN.getRoleName());
        verify(surveySettingsService).deleteLogo();
    }

    @Test
    void uploadLogo_ShouldRejectNonAdminUser() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", new byte[]{1, 2, 3});
        doThrow(new SecurityException("Forbidden"))
                .when(claimsPrincipalService)
                .ensureRole(Role.ADMIN.getRoleName());

        assertThatThrownBy(() -> surveySettingsController.uploadLogo(file))
                .isInstanceOf(SecurityException.class);

        verify(surveySettingsService, never()).uploadLogo(any());
    }

    @Test
    void deleteLogo_ShouldRejectNonAdminUser() {
        doThrow(new SecurityException("Forbidden"))
                .when(claimsPrincipalService)
                .ensureRole(Role.ADMIN.getRoleName());

        assertThatThrownBy(() -> surveySettingsController.deleteLogo())
                .isInstanceOf(SecurityException.class);

        verify(surveySettingsService, never()).deleteLogo();
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
