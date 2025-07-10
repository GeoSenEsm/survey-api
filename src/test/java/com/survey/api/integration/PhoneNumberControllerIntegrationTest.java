package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.PhoneNumberDtoIn;
import com.survey.application.dtos.PhoneNumberDtoOut;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.PhoneNumber;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.PhoneNumberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class PhoneNumberControllerIntegrationTest {
    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final TestUtils testUtils;
    private final PhoneNumberRepository phoneNumberRepository;
    private final String VALID_PHONE_NUMBER_1 = "+48123456789";
    private final String VALID_PHONE_NUMBER_2 = "123456789";
    private final String NAME_1 = "Alice";
    private final String NAME_2 = "Bob";

    @Autowired
    public PhoneNumberControllerIntegrationTest(WebTestClient webTestClient, IdentityUserRepository userRepository, TestUtils testUtils, PhoneNumberRepository phoneNumberRepository) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.testUtils = testUtils;
        this.phoneNumberRepository = phoneNumberRepository;
    }

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
        phoneNumberRepository.deleteAll();
    }
    @Test
    void createPhoneNumber_ValidData_ReturnsCreated() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        PhoneNumberDtoIn dtoIn = new PhoneNumberDtoIn("John Doe", VALID_PHONE_NUMBER_1);

        webTestClient.post().uri("/api/phonenumber")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dtoIn)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PhoneNumberDtoOut.class)
                .value(responseDto -> {
                    assertThat(responseDto.getId()).isNotNull();
                    assertThat(responseDto.getName()).isEqualTo(dtoIn.getName());
                    assertThat(responseDto.getNumber()).isEqualTo(dtoIn.getNumber());
                });

        assertThat(phoneNumberRepository.count()).isEqualTo(1);
    }

    @Test
    void createPhoneNumber_InvalidData_ReturnsBadRequest() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        PhoneNumberDtoIn invalidDto = new PhoneNumberDtoIn(null, "invalid-phone");

        webTestClient.post().uri("/api/phonenumber")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(phoneNumberRepository.count()).isEqualTo(0);
    }

    @Test
    void getAllPhoneNumbers_ReturnsAllRecords() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        phoneNumberRepository.save(new PhoneNumber(UUID.randomUUID(), NAME_1, VALID_PHONE_NUMBER_1));
        phoneNumberRepository.save(new PhoneNumber(UUID.randomUUID(), NAME_2, VALID_PHONE_NUMBER_2));

        webTestClient.get().uri("/api/phonenumber")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PhoneNumberDtoOut.class)
                .hasSize(2)
                .value(responseList -> {
                    assertThat(responseList)
                            .extracting(PhoneNumberDtoOut::getName)
                            .containsExactlyInAnyOrder(NAME_1, NAME_2);
                    assertThat(responseList)
                            .extracting(PhoneNumberDtoOut::getNumber)
                            .containsExactlyInAnyOrder(VALID_PHONE_NUMBER_1, VALID_PHONE_NUMBER_2);
                });
    }

    @Test
    void getAllPhoneNumbers_NoRecords_ReturnsEmptyList() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        webTestClient.get().uri("/api/phonenumber")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PhoneNumberDtoOut.class)
                .hasSize(0);
    }

    @Test
    void getPhoneNumberById_ExistingId_ReturnsRecord() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        PhoneNumber saved = phoneNumberRepository.saveAndFlush(new PhoneNumber(null, NAME_1, VALID_PHONE_NUMBER_1));
        UUID existingId = saved.getId();

        webTestClient.get()
                .uri("/api/phonenumber/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhoneNumberDtoOut.class)
                .value(responseDto -> {
                    assertThat(responseDto.getId()).isEqualTo(existingId);
                    assertThat(responseDto.getName()).isEqualTo(NAME_1);
                    assertThat(responseDto.getNumber()).isEqualTo(VALID_PHONE_NUMBER_1);
                });
    }

    @Test
    void getPhoneNumberById_NonExistingId_ReturnsNotFound() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        UUID nonExistingId = UUID.randomUUID();

        webTestClient.get().uri("/api/phonenumber/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePhoneNumber_ExistingIdValidData_ReturnsUpdatedRecord() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        PhoneNumber saved = phoneNumberRepository.saveAndFlush(new PhoneNumber(null, NAME_1, VALID_PHONE_NUMBER_1));
        UUID existingId = saved.getId();

        PhoneNumberDtoIn updateDto = new PhoneNumberDtoIn(NAME_2, VALID_PHONE_NUMBER_2);

        webTestClient.put().uri("/api/phonenumber/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhoneNumberDtoOut.class)
                .value(responseDto -> {
                    assertThat(responseDto.getId()).isEqualTo(existingId);
                    assertThat(responseDto.getName()).isEqualTo(updateDto.getName());
                    assertThat(responseDto.getNumber()).isEqualTo(updateDto.getNumber());
                });

        PhoneNumber updatedInDb = phoneNumberRepository.findById(existingId).orElseThrow();
        assertThat(updatedInDb.getName()).isEqualTo(updateDto.getName());
        assertThat(updatedInDb.getNumber()).isEqualTo(updateDto.getNumber());
    }

    @Test
    void updatePhoneNumber_NonExistingId_ReturnsNotFound() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        UUID nonExistingId = UUID.randomUUID();
        PhoneNumberDtoIn updateDto = new PhoneNumberDtoIn("Bob", VALID_PHONE_NUMBER_1);

        webTestClient.put().uri("/api/phonenumber/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isNotFound();

        assertThat(phoneNumberRepository.count()).isEqualTo(0);
    }

    @Test
    void updatePhoneNumber_ExistingIdInvalidData_ReturnsBadRequest() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        PhoneNumber saved = phoneNumberRepository.saveAndFlush(new PhoneNumber(null, NAME_1, VALID_PHONE_NUMBER_1));
        UUID existingId = saved.getId();

        String INVALID_PHONE_NUMBER = "123";
        PhoneNumberDtoIn invalidUpdateDto = new PhoneNumberDtoIn(NAME_1, INVALID_PHONE_NUMBER);

        webTestClient.put().uri("/api/phonenumber/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUpdateDto)
                .exchange()
                .expectStatus().isBadRequest();

        PhoneNumber originalInDb = phoneNumberRepository.findById(existingId).orElseThrow();
        assertThat(originalInDb.getName()).isEqualTo(NAME_1);
    }

    @Test
    void deletePhoneNumber_ExistingId_ReturnsNoContent() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        PhoneNumber saved = phoneNumberRepository.saveAndFlush(new PhoneNumber(null, NAME_1, VALID_PHONE_NUMBER_1));
        UUID existingId = saved.getId();

        webTestClient.delete().uri("/api/phonenumber/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();

        assertThat(phoneNumberRepository.findById(existingId)).isEmpty();
    }

    @Test
    void deletePhoneNumber_NonExistingId_ReturnsNotFound() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        UUID nonExistingId = UUID.randomUUID();

        webTestClient.delete().uri("/api/phonenumber/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

}
