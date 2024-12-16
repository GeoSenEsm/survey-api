package com.survey.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;
import com.survey.application.dtos.surveyDtos.ChangePasswordDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class AuthenticationControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final ObjectMapper objectMapper;
    private final IdentityUserRepository userRepository;
    private final TestUtils testUtils;
    private final PasswordEncoder passwordEncoder;
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private static final String RESPONDENT_PASSWORD = "testRespondentPassword!";
    private static final String RESPONDENT2_PASSWORD = "testRespondent2Password!";
    private static final String NEW_VALID_RESPONDENT_PASSWORD = "!newTestRespondentPassword1";
    private static final String NEW_INVALID_RESPONDENT_PASSWORD = "!newPassword";


    @Autowired
    public AuthenticationControllerIntegrationTest(WebTestClient webTestClient, ObjectMapper objectMapper,
                                                   IdentityUserRepository identityUserRepository, TestUtils testUtils, PasswordEncoder passwordEncoder){

        this.webTestClient = webTestClient;
        this.objectMapper = objectMapper;
        this.userRepository = identityUserRepository;
        this.testUtils = testUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }

    @Test
    public void testLoginForRespondentsForNonExistingUser() throws JsonProcessingException {
        LoginDto dto = LoginDto
                .builder()
                .withUsername("this user does not exist")
                .withPassword("test password")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        webTestClient.post().uri("/api/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testLoginForRespondentsForExistingUserButWrongPassword() throws JsonProcessingException{
        String randomUsername = UUID.randomUUID().toString();
        testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        LoginDto dto = LoginDto
                .builder()
                .withUsername(randomUsername)
                .withPassword("wrong password")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        webTestClient.post().uri("/api/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testLoginForRespondentsWithCorrectCredentials() throws JsonProcessingException{
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        LoginDto dto = LoginDto
                .builder()
                .withUsername(respondent.getUsername())
                .withPassword(RESPONDENT_PASSWORD)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        webTestClient.post().uri("/api/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testLoginForRespondentsForEmptyUsernameAndPassword() throws JsonProcessingException{
        LoginDto dto = LoginDto
                .builder()
                .build();

        String json = objectMapper.writeValueAsString(dto);

        webTestClient.post().uri("/api/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void testCreateRespondentsAccountsWithValidAdminCredentials() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        CreateRespondentsAccountsDto dto = new CreateRespondentsAccountsDto(3);

        webTestClient.post().uri("/api/authentication/respondents")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3);
    }

    @Test
    public void testCreateRespondentsAccountsWithInvalidCredentials() {
        CreateRespondentsAccountsDto dto = new CreateRespondentsAccountsDto(3);

        webTestClient.post().uri("/api/authentication/respondents")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testAdminLoginWithCorrectCredentials() throws JsonProcessingException {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);

        LoginDto dto = LoginDto
                .builder()
                .withUsername(admin.getUsername())
                .withPassword(ADMIN_PASSWORD)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        webTestClient.post().uri("/api/authentication/login/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testAdminLoginWithInvalidCredentials() throws JsonProcessingException {
        LoginDto dto = LoginDto
                .builder()
                .withUsername("admin")
                .withPassword("wrongPassword")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        webTestClient.post().uri("/api/authentication/login/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testAdminLoginWithMissingCredentials() throws JsonProcessingException {
        LoginDto dto = LoginDto
                .builder()
                .build();

        String json = objectMapper.writeValueAsString(dto);

        webTestClient.post().uri("/api/authentication/login/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void editRespondentPassword_ShouldReturnOk_WhenAdminUpdatesRespondentPassword() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);

        ChangePasswordDto changePasswordDto = createChangePasswordDto(null, NEW_VALID_RESPONDENT_PASSWORD);

        webTestClient.patch()
                .uri("api/authentication/" + respondent.getId() + "/password")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(changePasswordDto)
                .exchange()
                .expectStatus().isOk();

        assertPasswordChanged(respondent.getId(), NEW_VALID_RESPONDENT_PASSWORD);
    }

    @Test
    void editRespondentPassword_ShouldReturnOk_WhenAdminUpdatesOwnPassword() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        ChangePasswordDto changePasswordDto = createChangePasswordDto(ADMIN_PASSWORD, NEW_VALID_RESPONDENT_PASSWORD);

        webTestClient.patch()
                .uri("api/authentication/" + admin.getId() + "/password")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(changePasswordDto)
                .exchange()
                .expectStatus().isOk();

        assertPasswordChanged(admin.getId(), NEW_VALID_RESPONDENT_PASSWORD);
    }

    @Test
    void updateRespondentPassword_ShouldReturnOk_WhenRespondentUpdatesOwnPassword() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        ChangePasswordDto changePasswordDto = createChangePasswordDto(RESPONDENT_PASSWORD, NEW_VALID_RESPONDENT_PASSWORD);

        webTestClient.patch()
                .uri("api/authentication/" + respondent.getId() + "/password")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(changePasswordDto)
                .exchange()
                .expectStatus().isOk();

        assertPasswordChanged(respondent.getId(), NEW_VALID_RESPONDENT_PASSWORD);
    }

    @Test
    void updateRespondentPassword_ShouldReturnBadRequest_WhenOldPasswordIsIncorrect() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        ChangePasswordDto changePasswordDto = createChangePasswordDto("invalid_old_password", NEW_VALID_RESPONDENT_PASSWORD);

        webTestClient.patch()
                .uri("api/authentication/" + respondent.getId() + "/password")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(changePasswordDto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateRespondentPassword_ShouldReturnForbidden_WhenRespondentTriesToUpdateAnotherRespondentsPassword() {
        IdentityUser respondent1 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondent1Token = testUtils.authenticateAndGenerateToken(respondent1, RESPONDENT_PASSWORD);

        IdentityUser respondent2 = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT2_PASSWORD);

        ChangePasswordDto changePasswordDto = createChangePasswordDto(RESPONDENT2_PASSWORD, NEW_VALID_RESPONDENT_PASSWORD);

        webTestClient.patch()
                .uri("api/authentication/" + respondent2.getId() + "/password")
                .header("Authorization", "Bearer " + respondent1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(changePasswordDto)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void updateRespondentPassword_ShouldReturnBadRequest_WhenNewPasswordDoesNotMeetComplexityRequirements() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        ChangePasswordDto changePasswordDto = createChangePasswordDto(RESPONDENT_PASSWORD, NEW_INVALID_RESPONDENT_PASSWORD);

        webTestClient.patch()
                .uri("api/authentication/" + respondent.getId() + "/password")
                .header("Authorization", "Bearer " + respondentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(changePasswordDto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private void assertPasswordChanged(UUID userId, String expectedPassword) {
        IdentityUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found after password update"));

        assertTrue(passwordEncoder.matches(expectedPassword, user.getPasswordHash()));
    }
    private ChangePasswordDto createChangePasswordDto(String oldPassword, String newPassword){
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setOldPassword(oldPassword);
        changePasswordDto.setNewPassword(newPassword);
        return changePasswordDto;
    }

}
