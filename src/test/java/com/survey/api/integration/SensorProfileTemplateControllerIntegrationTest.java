package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorParameterDefinitionDto;
import com.survey.application.dtos.SensorProfileTemplateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
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
 * ("kestrel", "pc_60fw", "ruuvi", "inkbird_ibs_th1") that no other integration test installs,
 * since the Testcontainers database is shared across the whole suite run. ("xiaomi" is
 * deliberately avoided: several other classes create a sensor_type row with that code directly
 * via {@code TestUtils.getOrCreateXiaomiSensorType()}, so installing it here would 400.)
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

    /**
     * Nothing is pre-seeded any more (see V30/V31 — the parameter-definition INSERTs were
     * removed once this create-on-demand path landed): a used parameter only starts existing the
     * first time some installed template needs it, and a second template needing the same code
     * (by design, "kestrel", "ruuvi" and "inkbird_ibs_th1" all produce "temperature"/"humidity")
     * must reuse the same row rather than colliding on the (name, unit) uniqueness constraint or
     * duplicating it.
     */
    @Test
    void installTemplate_createsUsedParametersOnDemandWithManualSourceAndReusesThemAcrossTemplates() {
        // Uses "ruuvi" and "inkbird_ibs_th1" — not "xiaomi": several other integration test
        // classes (e.g. SensorMacControllerIntegrationTest) call
        // TestUtils.getOrCreateXiaomiSensorType(), which inserts a sensor_type row with code
        // "xiaomi" directly into the repository, bypassing the install endpoint entirely — since
        // this Testcontainers database is shared across the whole suite run, installing "xiaomi"
        // here 400s with "already installed" whenever one of those classes happens to run first.
        // "ruuvi"/"inkbird_ibs_th1" aren't touched outside this class. Also, because "kestrel"
        // (installed by another test in this class) maps the same "temperature"/"humidity" codes
        // and JUnit gives no ordering guarantee across the shared database, this asserts the
        // *change* each install makes (source-count delta, id stability) rather than an absolute
        // count, which would be wrong if "kestrel" happened to run first.
        String adminToken = adminToken();

        int temperatureSourcesBefore = sourceCountIfPresent(getSensorDataSettings(adminToken), "temperature");

        webTestClient.post()
                .uri("/api/sensorprofiles/templates/ruuvi/install")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isCreated();

        SurveySensorDataSettingsDto afterFirstInstall = getSensorDataSettings(adminToken);
        SensorParameterDefinitionDto temperature = findParameter(afterFirstInstall, "temperature");
        SensorParameterDefinitionDto humidity = findParameter(afterFirstInstall, "humidity");
        assertThat(temperature.unit()).isEqualTo("C");
        assertThat(humidity.unit()).isEqualTo("%");
        // +1 for ruuvi's own raw source, +1 more for manual if this was the very first template
        // ever to need "temperature" (manual is only added when the definition is newly created).
        assertThat(temperature.sources()).hasSize(temperatureSourcesBefore + (temperatureSourcesBefore == 0 ? 2 : 1));
        assertThat(temperature.sources()).anySatisfy(source -> assertThat(source.sensorTypeCode()).isEqualTo("manual"));
        assertThat(temperature.sources()).anySatisfy(source -> assertThat(source.sensorTypeCode()).isEqualTo("ruuvi"));

        webTestClient.post()
                .uri("/api/sensorprofiles/templates/inkbird_ibs_th1/install")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isCreated();

        SurveySensorDataSettingsDto afterSecondInstall = getSensorDataSettings(adminToken);
        assertThat(afterSecondInstall.parameters())
                .filteredOn(p -> p.code().equals("temperature") || p.code().equals("humidity"))
                .hasSize(2); // reused, not duplicated
        SensorParameterDefinitionDto temperatureAfterBoth = findParameter(afterSecondInstall, "temperature");
        assertThat(temperatureAfterBoth.id()).isEqualTo(temperature.id());
        assertThat(temperatureAfterBoth.sources()).hasSize(temperature.sources().size() + 1); // + inkbird_ibs_th1
        assertThat(temperatureAfterBoth.sources())
                .anySatisfy(source -> assertThat(source.sensorTypeCode()).isEqualTo("inkbird_ibs_th1"));
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

    private SurveySensorDataSettingsDto getSensorDataSettings(String adminToken) {
        return webTestClient.get()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SurveySensorDataSettingsDto.class)
                .returnResult().getResponseBody();
    }

    private SensorParameterDefinitionDto findParameter(SurveySensorDataSettingsDto settings, String code) {
        return settings.parameters().stream()
                .filter(p -> p.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Parameter not found in response: " + code));
    }

    private int sourceCountIfPresent(SurveySensorDataSettingsDto settings, String code) {
        return settings.parameters().stream()
                .filter(p -> p.code().equals(code))
                .findFirst()
                .map(p -> p.sources().size())
                .orElse(0);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
