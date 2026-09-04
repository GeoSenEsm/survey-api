package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.services.SensorDataService;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorData;
import com.survey.domain.models.SensorDataParameterValue;
import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.models.SensorType;
import com.survey.domain.repository.SensorDataRepository;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@link SensorDataService#getSensorDataBatch} against a real Testcontainers-backed
 * database with enough rows to require multiple pages, so a regression to fetch-join-plus-offset
 * pagination (which loads the whole matching result set into memory instead of paging in SQL)
 * would surface as duplicated or dropped rows here.
 */
@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
class SensorDataPaginationIntegrationTest {
    private static final int TOTAL_ROWS = 12;
    private static final int PAGE_SIZE = 5;

    private final SensorDataService sensorDataService;
    private final SensorDataRepository sensorDataRepository;
    private final SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    private final TestUtils testUtils;

    @Autowired
    SensorDataPaginationIntegrationTest(
            SensorDataService sensorDataService,
            SensorDataRepository sensorDataRepository,
            SensorParameterDefinitionRepository sensorParameterDefinitionRepository,
            TestUtils testUtils) {
        this.sensorDataService = sensorDataService;
        this.sensorDataRepository = sensorDataRepository;
        this.sensorParameterDefinitionRepository = sensorParameterDefinitionRepository;
        this.testUtils = testUtils;
    }

    @BeforeEach
    void setUp() {
        sensorDataRepository.deleteAll();
    }

    @Test
    void getSensorDataBatch_PagesThroughAllRows_WithoutDuplicatesOrDrops() {
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "irrelevant");
        SensorType xiaomi = testUtils.getOrCreateXiaomiSensorType();
        // Nothing is pre-seeded any more (sensor_parameter_definition starts empty until some
        // installed template needs a code), so this test creates its own row rather than assuming
        // "temperature" already exists. Uses a random per-test code instead of the real
        // "temperature" code: writing that row directly via the repository (bypassing
        // SensorTypeParameterService.ensureManualSource, which the create-on-demand paths always
        // go through) would leave a "temperature" definition with zero sources sitting in this
        // shared Testcontainers database — a later test's real "temperature" template install
        // would then reuse this bare row via findByCode() instead of creating its own, silently
        // skipping its own manual-source guarantee too. A one-off code sidesteps that collision
        // entirely; the value below is what pagination is actually exercising, not the identity of
        // the definition it's attached to.
        SensorParameterDefinition temperature = new SensorParameterDefinition();
        temperature.setCode("pagination_test_" + UUID.randomUUID());
        temperature.setName("Pagination Test Temperature " + UUID.randomUUID());
        temperature.setDataType("decimal");
        temperature.setUnit("C");
        temperature = sensorParameterDefinitionRepository.save(temperature);

        OffsetDateTime base = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        List<UUID> insertedIdsInOrder = new ArrayList<>();
        for (int i = 0; i < TOTAL_ROWS; i++) {
            SensorData sensorData = new SensorData()
                    .setRespondent(respondent)
                    .setDateTime(base.plusSeconds(i))
                    .setSourceSensorType(xiaomi)
                    .setSource(xiaomi.getCode());
            SensorDataParameterValue value = new SensorDataParameterValue();
            value.setSensorData(sensorData);
            value.setParameterDefinition(temperature);
            value.setValue("2" + i + ".0");
            sensorData.getValues().add(value);

            insertedIdsInOrder.add(sensorDataRepository.save(sensorData).getId());
        }

        List<ResponseSensorDataDto> collected = new ArrayList<>();
        int offset = 0;
        List<ResponseSensorDataDto> page;
        do {
            page = sensorDataService.getSensorDataBatch(null, null, respondent.getId(), offset, PAGE_SIZE);
            collected.addAll(page);
            offset += PAGE_SIZE;
        } while (!page.isEmpty());

        assertThat(collected).hasSize(TOTAL_ROWS);
        assertThat(collected).extracting(ResponseSensorDataDto::getId).doesNotHaveDuplicates();
        assertThat(collected).extracting(ResponseSensorDataDto::getId)
                .containsExactlyElementsOf(insertedIdsInOrder);
        assertThat(collected).isSortedAccordingTo(
                (a, b) -> a.getDateTime().compareTo(b.getDateTime()));
    }
}
