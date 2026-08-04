package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorProfileTemplateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.InitialSurveyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the happy path and edge cases of the sensor profile template install feature
 * (GET /api/sensorprofiles/templates, POST .../install), which previously had only incidental
 * coverage via other tests' setup or the setup-lock rejection path. Uses template codes
 * ("kestrel", "pc_60fw") that no other integration test installs, since the Testcontainers
 * database is shared across the whole suite run.
 *
 * <p>Installing is rejected once the initial survey is published, and that state is a global
 * singleton in the shared database that other classes leave published (e.g.
 * InitialSurveyControllerIntegrationTest only resets in {@code @BeforeEach}). So this class
 * resets it before and after each test rather than relying on class ordering, the same way
 * {@link SensorDataSetupLockIntegrationTest} does.
 */
@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
class SensorProfileTemplateControllerIntegrationTest {
    private static final String ADMIN_PASSWORD = "testAdminPassword";

    private final WebTestClient webTestClient;
    private final TestUtils testUtils;
    private final InitialSurveyRepository initialSurveyRepository;

    @Autowired
    SensorProfileTemplateControllerIntegrationTest(
            WebTestClient webTestClient,
            TestUtils testUtils,
            InitialSurveyRepository initialSurveyRepository) {
        this.webTestClient = webTestClient;
        this.testUtils = testUtils;
        this.initialSurveyRepository = initialSurveyRepository;
    }

    @BeforeEach
    @AfterEach
    void resetInitialSurvey() {
        initialSurveyRepository.deleteAll();
    }

    @Test
    void installTemplate_happyPath_createsAPublishedProfileAndFlipsTheInstalledFlag() {
        String adminToken = adminToken();

        SensorProfileTemplateDto before = findTemplate(listTemplates(adminToken), "kestrel");
        assertThat(before.parameterCodes()).containsExactlyInAnyOrder("temperature", "humidity");
        assertThat(before.installed()).isFalse();

        SensorTypeDtoOut installed = webTestClient.post()
                .uri("/api/sensorprofiles/templates/kestrel/install")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult().getResponseBody();

        assertThat(installed).isNotNull();
        assertThat(installed.getCode()).isEqualTo("kestrel");
        assertThat(installed.getName()).isEqualTo("Kestrel");
        assertThat(installed.getIntegrationMode()).isEqualTo("profile");

        List<SensorGattProfileDto> profiles = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensorprofiles")
                        .queryParam("sensorTypeId", installed.getId())
                        .build())
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorGattProfileDto.class)
                .returnResult().getResponseBody();

        assertThat(profiles).hasSize(1);
        assertThat(profiles.get(0).status()).isEqualTo("published");
        assertThat(profiles.get(0).sensorTypeCode()).isEqualTo("kestrel");

        assertThat(findTemplate(listTemplates(adminToken), "kestrel").installed()).isTrue();
    }

    @Test
    void installTemplate_rejectsReinstallOfAnAlreadyInstalledTemplate() {
        String adminToken = adminToken();

        ensureInstalled(adminToken, "pc_60fw");

        webTestClient.post()
                .uri("/api/sensorprofiles/templates/pc_60fw/install")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("already installed"));
    }

    @Test
    void installTemplate_returnsNotFoundForAnUnknownTemplateCode() {
        String adminToken = adminToken();

        webTestClient.post()
                .uri("/api/sensorprofiles/templates/does-not-exist/install")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listTemplates_isRejectedForNonAdminUsers() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "irrelevantPassword");
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, "irrelevantPassword");

        webTestClient.get()
                .uri("/api/sensorprofiles/templates")
                .header("Authorization", bearer(respondentToken))
                .exchange()
                .expectStatus().isForbidden();
    }

    /**
     * A template install is irreversible within a suite run and JUnit guarantees no ordering
     * between the methods here, so this asserts the end state instead of a 201: the template is
     * installed, whether this call or an earlier test did it.
     */
    private void ensureInstalled(String adminToken, String templateCode) {
        webTestClient.post()
                .uri("/api/sensorprofiles/templates/{templateCode}/install", templateCode)
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().value(status -> assertThat(status).isIn(201, 400));

        assertThat(findTemplate(listTemplates(adminToken), templateCode).installed()).isTrue();
    }

    private String adminToken() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        return testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
    }

    private List<SensorProfileTemplateDto> listTemplates(String adminToken) {
        return webTestClient.get()
                .uri("/api/sensorprofiles/templates")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorProfileTemplateDto.class)
                .returnResult().getResponseBody();
    }

    private SensorProfileTemplateDto findTemplate(List<SensorProfileTemplateDto> templates, String code) {
        return templates.stream()
                .filter(template -> template.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Template not found in response: " + code));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
