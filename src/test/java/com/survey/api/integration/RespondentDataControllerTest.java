package com.survey.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.security.TokenProvider;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentData;
import com.survey.domain.models.enums.Gender;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.RespondentDataRepository;
import jakarta.persistence.EntityManager;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class RespondentDataControllerTest {
    private final WebTestClient webTestClient;
    private final ObjectMapper objectMapper;
    private final IdentityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RespondentDataRepository respondentDataRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public RespondentDataControllerTest(WebTestClient webTestClient, ObjectMapper objectMapper, IdentityUserRepository userRepository, PasswordEncoder passwordEncoder,
                                        TokenProvider tokenProvider, RespondentDataRepository respondentDataRepository,
                                        AuthenticationManager authenticationManager) {
        this.webTestClient = webTestClient;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.respondentDataRepository = respondentDataRepository;
        this.authenticationManager = authenticationManager;
    }

    @Test
    void getFromUserContextShouldReturnARespondentDataFromTheLoggedInUserContext(){
        IdentityUser identityUser = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole("Respondent")
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode("pswd"));

        identityUser = userRepository.saveAndFlush(identityUser);

        RespondentData respondentData= new RespondentData()
                .setIdentityUserId(identityUser.getId())
                .setGender(Gender.male)
                .setAgeCategoryId(1)
                .setEducationCategoryId(1)
                .setHealthConditionId(1)
                .setLifeSatisfactionId(1)
                .setMedicationUseId(1)
                .setOccupationCategoryId(1)
                .setQualityOfSleepId(1)
                .setGreeneryAreaCategoryId(1)
                .setStressLevelId(1);

        respondentDataRepository.saveAndFlush(respondentData);

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(identityUser.getUsername(),
                        "pswd"));

        String token = tokenProvider.generateToken(authentication);

        webTestClient.get().uri("/api/respondents")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();
    }
}
