package com.survey.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentData;
import com.survey.domain.models.enums.Gender;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.RespondentDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

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

        var bodyRespondent = webTestClient.get().uri("/api/respondents")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RespondentDataDto.class)
                .returnResult()
                .getResponseBody();

        RespondentDataDto expectedDto = new RespondentDataDto()
                .setId(respondentData.getId())
                .setIdentityUserId(identityUser.getId())
                .setUsername(identityUser.getUsername())
                .setGender(respondentData.getGender().toString())
                .setAgeCategoryId(respondentData.getAgeCategoryId())
                .setEducationCategoryId(respondentData.getEducationCategoryId())
                .setHealthConditionId(respondentData.getHealthConditionId())
                .setLifeSatisfactionId(respondentData.getLifeSatisfactionId())
                .setMedicationUseId(respondentData.getMedicationUseId())
                .setOccupationCategoryId(respondentData.getOccupationCategoryId())
                .setQualityOfSleepId(respondentData.getQualityOfSleepId())
                .setGreeneryAreaCategoryId(respondentData.getGreeneryAreaCategoryId())
                .setStressLevelId(respondentData.getStressLevelId());

        assertThat(bodyRespondent).isNotNull();
        assertThat(bodyRespondent).isEqualToComparingFieldByField(expectedDto);
    }

    @Test
    void getFromUserContextShouldGiveNotFoundWhenTheRespondentDataWasNotCreatedYet(){
        IdentityUser identityUser = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole("Respondent")
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode("pswd"));

        identityUser = userRepository.saveAndFlush(identityUser);


        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(identityUser.getUsername(),
                        "pswd"));

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

    @Test
    void createRespondentDataForTheValidDataShouldReturnCreatedAndAddRespondentDataToTheDatabase(){
        IdentityUser identityUser = new IdentityUser()
                .setId(UUID.randomUUID())
                .setRole("Respondent")
                .setUsername(UUID.randomUUID().toString())
                .setPasswordHash(passwordEncoder.encode("pswd"));

        identityUser = userRepository.saveAndFlush(identityUser);

        CreateRespondentDataDto createDto = new CreateRespondentDataDto()
                .setGender("male")
                .setAgeCategoryId(1)
                .setEducationCategoryId(1)
                .setHealthConditionId(1)
                .setLifeSatisfactionId(1)
                .setMedicationUseId(1)
                .setOccupationCategoryId(1)
                .setQualityOfSleepId(1)
                .setGreeneryAreaCategoryId(1)
                .setStressLevelId(1);

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(identityUser.getUsername(),
                        "pswd"));

        String token = tokenProvider.generateToken(authentication);

        var bodyRespondent = webTestClient.post().uri("/api/respondents")
                .header("Authorization", "Bearer " + token)
                .bodyValue(createDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(RespondentDataDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(bodyRespondent.getGender()).isEqualTo("male");
        assertThat(bodyRespondent.getAgeCategoryId()).isEqualTo(1);
        assertThat(bodyRespondent.getEducationCategoryId()).isEqualTo(1);
        assertThat(bodyRespondent.getHealthConditionId()).isEqualTo(1);
        assertThat(bodyRespondent.getLifeSatisfactionId()).isEqualTo(1);
        assertThat(bodyRespondent.getMedicationUseId()).isEqualTo(1);
        assertThat(bodyRespondent.getOccupationCategoryId()).isEqualTo(1);
        assertThat(bodyRespondent.getQualityOfSleepId()).isEqualTo(1);
        assertThat(bodyRespondent.getGreeneryAreaCategoryId()).isEqualTo(1);
        assertThat(bodyRespondent.getStressLevelId()).isEqualTo(1);

        var respondentData = respondentDataRepository.findByIdentityUserId(identityUser.getId());
        assertThat(respondentData).isNotNull();
        assertThat(respondentData.getGender()).isEqualTo(Gender.male);
        assertThat(respondentData.getIdentityUserId()).isEqualTo(identityUser.getId());
        assertThat(respondentData.getAgeCategoryId()).isEqualTo(1);
        assertThat(respondentData.getEducationCategoryId()).isEqualTo(1);
        assertThat(respondentData.getHealthConditionId()).isEqualTo(1);
        assertThat(respondentData.getLifeSatisfactionId()).isEqualTo(1);
        assertThat(respondentData.getMedicationUseId()).isEqualTo(1);
        assertThat(respondentData.getOccupationCategoryId()).isEqualTo(1);
        assertThat(respondentData.getQualityOfSleepId()).isEqualTo(1);
        assertThat(respondentData.getGreeneryAreaCategoryId()).isEqualTo(1);
        assertThat(respondentData.getStressLevelId()).isEqualTo(1);
    }
}
