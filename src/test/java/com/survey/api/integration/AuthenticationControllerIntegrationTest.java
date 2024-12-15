package com.survey.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;
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

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class AuthenticationControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final ObjectMapper objectMapper;
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TestUtils testUtils;
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private static final String RESPONDENT_PASSWORD = "testRespondentPassword";


    @Autowired
    public AuthenticationControllerIntegrationTest(WebTestClient webTestClient, ObjectMapper objectMapper,
                                                   IdentityUserRepository identityUserRepository,
                                                   PasswordEncoder passwordEncoder, TestUtils testUtils){

        this.webTestClient = webTestClient;
        this.objectMapper = objectMapper;
        this.userRepository = identityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.testUtils = testUtils;
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
    public void testCreateRespondentsAccountsWithValidAdminCredentials() throws JsonProcessingException {
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
    public void testCreateRespondentsAccountsWithInvalidCredentials() throws JsonProcessingException {
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


}
