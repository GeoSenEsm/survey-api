package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.SensorDataDto;
import com.survey.application.dtos.SensorDataValueDto;
import com.survey.application.dtos.SensorParameterDefinitionCreateDto;
import com.survey.application.dtos.SensorParameterDefinitionDto;
import com.survey.application.dtos.SensorParameterDefinitionEditDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SensorTypeSettingDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.UseSensorTypeParameterDto;
import com.survey.domain.models.IdentityUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage of the raw-parameter-catalog -> "use" (promotion) -> single-row-edit flow
 * that replaced the old bulk parameter PUT (see {@link SurveySettingsServiceImplTest} for the
 * unit-level uniqueness checks).
 */
@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
class SensorTypeParameterIntegrationTest {
    private static final String ADMIN_PASSWORD = "testAdminPassword";

    private final WebTestClient webTestClient;
    private final TestUtils testUtils;

    @Autowired
    SensorTypeParameterIntegrationTest(WebTestClient webTestClient, TestUtils testUtils) {
        this.webTestClient = webTestClient;
        this.testUtils = testUtils;
    }

    @Test
    void rawParameter_canBePromotedIntoUsedSensorDataAndThenEdited() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("promote_" + suffix, "Promote " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto rawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("raw_temp_" + suffix, "Raw Temp", "decimal", "C"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(rawParameter.usedParameterId()).isNull();

        String usedName = "Temperature " + suffix;
        SensorTypeParameterDto usedRawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, usedName, "decimal", "C"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(usedRawParameter.usedParameterId()).isNotNull();

        SurveySensorDataSettingsDto settings = webTestClient.get()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SurveySensorDataSettingsDto.class)
                .returnResult()
                .getResponseBody();

        SensorParameterDefinitionDto usedParameter = settings.parameters().stream()
                .filter(p -> p.id().equals(usedRawParameter.usedParameterId()))
                .findFirst()
                .orElseThrow();
        assertThat(usedParameter.name()).isEqualTo(usedName);
        // The physical source just promoted, plus the `manual` fallback every used parameter is
        // guaranteed to get automatically.
        assertThat(usedParameter.sources()).hasSize(2);
        assertThat(usedParameter.sources())
                .anySatisfy(source -> {
                    assertThat(source.sensorTypeCode()).isEqualTo(sensorType.getCode());
                    assertThat(source.rawParameterCode()).isEqualTo("raw_temp_" + suffix);
                });
        assertThat(usedParameter.sources()).anySatisfy(source -> assertThat(source.sensorTypeCode()).isEqualTo("manual"));

        // A harmless rename (no conflict) should succeed.
        webTestClient.put()
                .uri("/api/surveysettings/sensordata/parameters/" + usedParameter.id())
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionEditDto(
                        "Renamed " + suffix, "decimal", "C2", usedParameter.displayOrder()))
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/api/surveysettings/sensordata/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionCreateDto(
                        "other_" + suffix, "Other " + suffix, "decimal", "x"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.put()
                .uri("/api/surveysettings/sensordata/parameters/" + usedParameter.id())
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionEditDto(
                        "Other " + suffix, "decimal", "x", usedParameter.displayOrder()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("already used"));
    }

    @Test
    void disablingSensorType_removesItsSourceButParameterSurvivesViaManualFallback() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("disable_" + suffix, "Disable " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto rawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("raw_temp_" + suffix, "Raw Temp", "decimal", "C"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto usedRawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, "Temperature " + suffix, "decimal", "C"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
        UUID usedParameterId = usedRawParameter.usedParameterId();
        assertThat(usedParameterId).isNotNull();

        SurveySensorDataSettingsDto beforeDisable = getSensorDataSettings(adminToken);
        assertThat(beforeDisable.parameters()).anyMatch(p -> usedParameterId.equals(p.id()));

        List<SensorTypeSettingDto> disabledSensorTypes = beforeDisable.sensorTypes().stream()
                .map(setting -> setting.sensorTypeCode().equals(sensorType.getCode())
                        ? disable(setting)
                        : setting)
                .toList();

        webTestClient.put()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SurveySensorDataSettingsWriteDto(beforeDisable.mode(), disabledSensorTypes))
                .exchange()
                .expectStatus().isOk();

        // Every used parameter also has `manual` wired as a guaranteed fallback source, so
        // disabling the sensor type behind its only *physical* source no longer strands it —
        // the parameter survives, now backed solely by that manual fallback.
        SurveySensorDataSettingsDto afterDisable = getSensorDataSettings(adminToken);
        SensorParameterDefinitionDto survivingParameter = afterDisable.parameters().stream()
                .filter(p -> usedParameterId.equals(p.id()))
                .findFirst()
                .orElseThrow();
        assertThat(survivingParameter.sources()).hasSize(1);
        assertThat(survivingParameter.sources().get(0).sensorTypeCode()).isEqualTo("manual");
    }

    @Test
    void unuseSensorTypeParameter_isRejectedWithConflictOnceLeavingItTrulySourcelessWithCollectedData() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "irrelevant");
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, "irrelevant");
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("unuselock_" + suffix, "Unuse lock " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto rawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("raw_temp_" + suffix, "Raw Temp", "decimal", "C"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto usedRawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, "Temperature " + suffix, "decimal", "C"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(usedRawParameter.usedParameterId()).isNotNull();
        assertThat(usedRawParameter.code()).isNotNull();

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", bearer(respondentToken))
                .bodyValue(List.of(new SensorDataDto(
                        OffsetDateTime.now(ZoneOffset.UTC),
                        sensorType.getCode(),
                        List.of(new SensorDataValueDto(usedRawParameter.code(), "21.0")))))
                .exchange()
                .expectStatus().isCreated();

        // The physical source is no longer this parameter's only one: every used parameter also
        // gets `manual` wired automatically, so unusing the physical source now simply succeeds —
        // the parameter survives via its manual fallback, with the collected reading untouched.
        webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/unuse")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk();

        SurveySensorDataSettingsDto afterFirstUnuse = getSensorDataSettings(adminToken);
        assertThat(afterFirstUnuse.parameters())
                .anyMatch(p -> p.id().equals(usedRawParameter.usedParameterId()));

        // Now unuse the manual fallback too, leaving the parameter truly sourceless — a reading
        // already exists for it, so the FK on sensor_data_parameter_value must reject the delete
        // instead of silently destroying that data.
        UUID manualSensorTypeId = webTestClient.get()
                .uri("/api/sensormac/types")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody()
                .stream()
                .filter(type -> "manual".equals(type.getCode()))
                .findFirst()
                .orElseThrow()
                .getId();

        UUID manualRawParameterId = webTestClient.get()
                .uri("/api/sensorprofiles/types/" + manualSensorTypeId + "/parameters")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody()
                .stream()
                .filter(raw -> usedRawParameter.usedParameterId().equals(raw.usedParameterId()))
                .findFirst()
                .orElseThrow()
                .id();

        webTestClient.post()
                .uri("/api/sensorprofiles/types/" + manualSensorTypeId + "/parameters/" + manualRawParameterId + "/unuse")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isEqualTo(org.springframework.http.HttpStatus.CONFLICT)
                .expectBody(String.class)
                .value(body -> assertThat(body).isNotBlank());

        SurveySensorDataSettingsDto afterFailedUnuse = getSensorDataSettings(adminToken);
        assertThat(afterFailedUnuse.parameters())
                .anyMatch(p -> p.id().equals(usedRawParameter.usedParameterId()));
    }

    @Test
    void deleteSensorType_isRejectedOnceSensorDataHasBeenCollectedForOneOfItsUsedParameters() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "irrelevant");
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, "irrelevant");
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("deletelock_" + suffix, "Delete lock " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto rawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("raw_temp_" + suffix, "Raw Temp", "decimal", "C"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto usedRawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, "Temperature " + suffix, "decimal", "C"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", bearer(respondentToken))
                .bodyValue(List.of(new SensorDataDto(
                        OffsetDateTime.now(ZoneOffset.UTC),
                        sensorType.getCode(),
                        List.of(new SensorDataValueDto(usedRawParameter.code(), "21.0")))))
                .exchange()
                .expectStatus().isCreated();

        // Deleting the sensor type would otherwise sever the collected reading's link to its
        // source type and, since this was the parameter's only source, leave the used parameter
        // permanently orphaned — the whole operation must be blocked instead.
        webTestClient.delete()
                .uri("/api/sensorprofiles/types/" + sensorType.getId())
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("already been collected"));

        SurveySensorDataSettingsDto afterFailedDelete = getSensorDataSettings(adminToken);
        assertThat(afterFailedDelete.parameters())
                .anyMatch(p -> p.id().equals(usedRawParameter.usedParameterId()));
    }

    /**
     * Deleting a used parameter only clears sensor_type_parameter.used_parameter_id
     * (ON DELETE SET NULL) rather than removing manual's raw row for that code — it survives,
     * orphaned. If a later parameter is created with the same code (a different sensor type's raw
     * parameter can legitimately share a code — raw catalogs are only unique per sensor type), it
     * must re-wire that orphaned row rather than either mistaking it for "manual already
     * guaranteed" (leaving the new parameter with no manual fallback) or trying to insert a second
     * (manual, code) row (which would violate the unique constraint on that pair).
     */
    @Test
    void ensureManualSource_reWiresAnOrphanedManualRowLeftByAPreviouslyDeletedParameterWithTheSameCode() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String sharedRawCode = "raw_orphan_" + suffix;

        SensorTypeDtoOut firstSensorType = createSensorType(adminToken, "orphanfirst_" + suffix);
        SensorTypeParameterDto firstRawParameter = createRawParameter(adminToken, firstSensorType, sharedRawCode);
        SensorTypeParameterDto firstUsedRawParameter = useParameter(
                adminToken, firstSensorType, firstRawParameter, "Orphan " + suffix, "C");
        UUID firstUsedParameterId = firstUsedRawParameter.usedParameterId();

        webTestClient.delete()
                .uri("/api/surveysettings/sensordata/parameters/" + firstUsedParameterId)
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isNoContent();

        SensorTypeDtoOut secondSensorType = createSensorType(adminToken, "orphansecond_" + suffix);
        SensorTypeParameterDto secondRawParameter = createRawParameter(adminToken, secondSensorType, sharedRawCode);
        SensorTypeParameterDto secondUsedRawParameter = useParameter(
                adminToken, secondSensorType, secondRawParameter, "Orphan again " + suffix, "C");
        UUID secondUsedParameterId = secondUsedRawParameter.usedParameterId();
        assertThat(secondUsedParameterId).isNotNull().isNotEqualTo(firstUsedParameterId);

        SurveySensorDataSettingsDto settings = getSensorDataSettings(adminToken);
        SensorParameterDefinitionDto secondUsedParameter = settings.parameters().stream()
                .filter(p -> p.id().equals(secondUsedParameterId))
                .findFirst()
                .orElseThrow();
        assertThat(secondUsedParameter.sources()).hasSize(2);
        assertThat(secondUsedParameter.sources())
                .anySatisfy(source -> assertThat(source.sensorTypeCode()).isEqualTo(secondSensorType.getCode()));
        assertThat(secondUsedParameter.sources())
                .anySatisfy(source -> assertThat(source.sensorTypeCode()).isEqualTo("manual"));
    }

    private SensorTypeDtoOut createSensorType(String adminToken, String code) {
        return webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto(code, code, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();
    }

    private SensorTypeParameterDto createRawParameter(String adminToken, SensorTypeDtoOut sensorType, String code) {
        return webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto(code, code, "decimal", "C"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
    }

    private SensorTypeParameterDto useParameter(
            String adminToken, SensorTypeDtoOut sensorType, SensorTypeParameterDto rawParameter, String name, String unit) {
        return webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, name, "decimal", unit))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
    }

    private SurveySensorDataSettingsDto getSensorDataSettings(String adminToken) {
        return webTestClient.get()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SurveySensorDataSettingsDto.class)
                .returnResult()
                .getResponseBody();
    }

    private static SensorTypeSettingDto disable(SensorTypeSettingDto setting) {
        return new SensorTypeSettingDto(
                setting.id(),
                setting.sensorTypeCode(),
                setting.sensorTypeName(),
                setting.integrationMode(),
                setting.adapterKey(),
                false,
                setting.connectionTimeoutSeconds(),
                setting.displayOrder());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
