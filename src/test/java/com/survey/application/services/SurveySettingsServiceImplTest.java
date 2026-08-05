package com.survey.application.services;

import com.survey.application.dtos.RespondentSensorAssignmentDto;
import com.survey.application.dtos.SensorParameterDefinitionCreateDto;
import com.survey.application.dtos.SensorParameterDefinitionDto;
import com.survey.application.dtos.SensorParameterDefinitionEditDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.SurveySettingsDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.models.SensorType;
import com.survey.domain.models.SensorTypeSetting;
import com.survey.domain.models.SurveySettings;
import com.survey.domain.models.SurveySensorSettings;
import com.survey.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveySettingsServiceImplTest {

    @Mock
    private SurveySettingsRepository surveySettingsRepository;
    @Mock
    private SurveySensorSettingsRepository surveySensorSettingsRepository;
    @Mock
    private SensorTypeSettingRepository sensorTypeSettingRepository;
    @Mock
    private SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    @Mock
    private SensorTypeRepository sensorTypeRepository;
    @Mock
    private RespondentSensorAssignmentRepository respondentSensorAssignmentRepository;
    @Mock
    private IdentityUserRepository identityUserRepository;
    @Mock
    private SensorMacRepository sensorMacRepository;
    @Mock
    private ClaimsPrincipalService claimsPrincipalService;
    @Mock
    private SensorGattProfileService sensorGattProfileService;
    @Mock
    private SensorDeviceSecretService sensorDeviceSecretService;
    @Mock
    private StorageService storageService;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private InitialSurveyService initialSurveyService;

    private SurveySettingsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SurveySettingsServiceImpl(
                surveySettingsRepository,
                surveySensorSettingsRepository,
                sensorTypeSettingRepository,
                sensorParameterDefinitionRepository,
                sensorTypeRepository,
                respondentSensorAssignmentRepository,
                identityUserRepository,
                sensorMacRepository,
                claimsPrincipalService,
                sensorGattProfileService,
                sensorDeviceSecretService,
                storageService,
                initialSurveyService,
                new SensorParameterDefinitionValidator(sensorParameterDefinitionRepository));
    }

    @Test
    void getSettings_returnsExistingRow() {
        when(surveySettingsRepository.findById(1))
                .thenReturn(Optional.of(new SurveySettings(1, false, ";", ",", null)));

        SurveySettingsDto dto = service.getSettings();

        assertThat(dto.showSendingPolicyCalendar()).isFalse();
        assertThat(dto.csvColumnSeparator()).isEqualTo(";");
        assertThat(dto.csvDecimalSeparator()).isEqualTo(",");
    }

    @Test
    void getSettings_createsDefaultWhenMissing() {
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.empty());
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SurveySettingsDto dto = service.getSettings();

        assertThat(dto.showSendingPolicyCalendar()).isTrue();
        assertThat(dto.csvColumnSeparator()).isEqualTo(",");
        assertThat(dto.csvDecimalSeparator()).isEqualTo(".");
        ArgumentCaptor<SurveySettings> captor = ArgumentCaptor.forClass(SurveySettings.class);
        verify(surveySettingsRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1);
    }

    @Test
    void updateSettings_persistsAllFields() {
        SurveySettings existing = new SurveySettings(1, true, ",", ".", null);
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.of(existing));
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SurveySettingsDto dto = service.updateSettings(new SurveySettingsDto(false, ";", ",", null));

        assertThat(dto.showSendingPolicyCalendar()).isFalse();
        assertThat(dto.csvColumnSeparator()).isEqualTo(";");
        assertThat(dto.csvDecimalSeparator()).isEqualTo(",");
    }

    @Test
    void updateSettings_rejectsIdenticalSeparators() {
        assertThatThrownBy(() -> service.updateSettings(new SurveySettingsDto(true, ",", ",", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be different");
    }

    @Test
    void updateSettings_doesNotOverwriteLogoPath() {
        SurveySettings existing = new SurveySettings(1, true, ",", ".", "/uploads/survey_settings/logo.png");
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.of(existing));
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SurveySettingsDto dto = service.updateSettings(
                new SurveySettingsDto(false, ";", ",", "/uploads/spoofed.png"));

        assertThat(dto.logoPath()).isEqualTo("/uploads/survey_settings/logo.png");
    }

    @Test
    void updateSensorDataSettings_rejectsChangesOnceTheInitialSurveyIsPublished() {
        when(initialSurveyService.isPublished()).thenReturn(true);

        assertThatThrownBy(() -> service.updateSensorDataSettings(
                new SurveySensorDataSettingsWriteDto("no_sensor_data", List.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been published");

        verify(surveySensorSettingsRepository, never()).save(any());
    }

    @Test
    void updateSensorDataSettings_allowsChangesWhileTheInitialSurveyIsNotYetPublished() {
        when(initialSurveyService.isPublished()).thenReturn(false);
        when(surveySensorSettingsRepository.findById(1))
                .thenReturn(Optional.of(new SurveySensorSettings(1, "no_sensor_data")));
        when(surveySensorSettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sensorTypeRepository.findAll()).thenReturn(List.of());
        when(sensorTypeSettingRepository.findAll()).thenReturn(List.of());
        when(sensorParameterDefinitionRepository.findAllOrderedWithSources()).thenReturn(List.of());
        when(respondentSensorAssignmentRepository.findAllOrdered()).thenReturn(List.of());

        SurveySensorDataSettingsDto dto = service.updateSensorDataSettings(
                new SurveySensorDataSettingsWriteDto("no_sensor_data", List.of()));

        assertThat(dto.mode()).isEqualTo("no_sensor_data");
    }

    @Test
    void updateSensorDataSettings_rejectsEmptySensorTypesWhenSettingsExist() {
        SensorType sensorType = new SensorType(UUID.randomUUID(), "xiaomi", "Xiaomi", "profile", null, null);
        SensorTypeSetting setting = new SensorTypeSetting(UUID.randomUUID(), sensorType, true, 30, 0);
        when(initialSurveyService.isPublished()).thenReturn(false);
        when(sensorTypeSettingRepository.findAll()).thenReturn(List.of(setting));

        assertThatThrownBy(() -> service.updateSensorDataSettings(
                new SurveySensorDataSettingsWriteDto("no_sensor_data", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sensorTypes must include");

        verify(surveySensorSettingsRepository, never()).save(any());
        verify(sensorTypeSettingRepository, never()).deleteAll();
    }

    @Test
    void createSensorParameterDefinition_savesNewActiveParameter() {
        when(initialSurveyService.isPublished()).thenReturn(false);
        when(sensorParameterDefinitionRepository.findByCode("temperature")).thenReturn(Optional.empty());
        when(sensorParameterDefinitionRepository.findAll()).thenReturn(List.of());
        when(sensorParameterDefinitionRepository.count()).thenReturn(0L);
        when(sensorParameterDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SensorParameterDefinitionDto dto = service.createSensorParameterDefinition(
                new SensorParameterDefinitionCreateDto("temperature", "Temperature", "decimal", "C", true));

        assertThat(dto.code()).isEqualTo("temperature");
        assertThat(dto.name()).isEqualTo("Temperature");
        assertThat(dto.active()).isTrue();
    }

    @Test
    void createSensorParameterDefinition_rejectsDuplicateCode() {
        when(initialSurveyService.isPublished()).thenReturn(false);
        when(sensorParameterDefinitionRepository.findByCode("temperature"))
                .thenReturn(Optional.of(new SensorParameterDefinition()));

        assertThatThrownBy(() -> service.createSensorParameterDefinition(
                new SensorParameterDefinitionCreateDto("temperature", "Temperature", "decimal", "C", true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(sensorParameterDefinitionRepository, never()).save(any());
    }

    @Test
    void createSensorParameterDefinition_rejectsConflictingNameUnitPair() {
        SensorParameterDefinition existing = new SensorParameterDefinition();
        existing.setId(UUID.randomUUID());
        existing.setCode("humidity");
        existing.setName("Temperature");
        existing.setUnit("C");
        when(initialSurveyService.isPublished()).thenReturn(false);
        when(sensorParameterDefinitionRepository.findByCode("temperature")).thenReturn(Optional.empty());
        when(sensorParameterDefinitionRepository.findAll()).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.createSensorParameterDefinition(
                new SensorParameterDefinitionCreateDto("temperature", "Temperature", "decimal", "C", true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already used");

        verify(sensorParameterDefinitionRepository, never()).save(any());
    }

    @Test
    void updateSensorParameterDefinition_updatesEditableFields() {
        UUID id = UUID.randomUUID();
        SensorParameterDefinition existing = new SensorParameterDefinition();
        existing.setId(id);
        existing.setCode("temperature");
        existing.setName("Temperature");
        existing.setUnit("C");
        existing.setActive(true);
        when(initialSurveyService.isPublished()).thenReturn(false);
        when(sensorParameterDefinitionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sensorParameterDefinitionRepository.findAll()).thenReturn(List.of(existing));
        when(sensorParameterDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SensorParameterDefinitionDto dto = service.updateSensorParameterDefinition(id,
                new SensorParameterDefinitionEditDto("Ambient Temperature", "decimal", "C", true, false, 3));

        assertThat(dto.name()).isEqualTo("Ambient Temperature");
        assertThat(dto.active()).isFalse();
        assertThat(dto.displayOrder()).isEqualTo(3);
    }

    @Test
    void updateSensorParameterDefinition_rejectsConflictingNameUnitPairWithAnotherRow() {
        UUID id = UUID.randomUUID();
        SensorParameterDefinition toUpdate = new SensorParameterDefinition();
        toUpdate.setId(id);
        toUpdate.setCode("temperature");
        toUpdate.setName("Temperature");
        toUpdate.setUnit("C");

        SensorParameterDefinition other = new SensorParameterDefinition();
        other.setId(UUID.randomUUID());
        other.setCode("humidity");
        other.setName("Humidity");
        other.setUnit("%");

        when(initialSurveyService.isPublished()).thenReturn(false);
        when(sensorParameterDefinitionRepository.findById(id)).thenReturn(Optional.of(toUpdate));
        when(sensorParameterDefinitionRepository.findAll()).thenReturn(List.of(toUpdate, other));

        assertThatThrownBy(() -> service.updateSensorParameterDefinition(id,
                new SensorParameterDefinitionEditDto("Humidity", "decimal", "%", true, true, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already used");

        verify(sensorParameterDefinitionRepository, never()).save(any());
    }

    @Test
    void updateAssignments_succeedsEvenAfterTheInitialSurveyIsPublished() {
        // Deliberately never stub initialSurveyService.isPublished(): unlike
        // updateSensorDataSettings, updateAssignments must not consult it at all.
        UUID respondentId = UUID.randomUUID();
        IdentityUser respondent = new IdentityUser()
                .setId(respondentId)
                .setUsername("respondent1")
                .setRole("Respondent");
        SensorType manual = new SensorType(UUID.randomUUID(), "manual", "Manual", "manual", null, null);
        when(sensorTypeRepository.findAll()).thenReturn(List.of(manual));
        when(identityUserRepository.findById(respondentId)).thenReturn(Optional.of(respondent));
        when(surveySensorSettingsRepository.findById(1))
                .thenReturn(Optional.of(new SurveySensorSettings(1, "no_sensor_data")));
        when(sensorTypeSettingRepository.findAllOrdered()).thenReturn(List.of());
        when(sensorParameterDefinitionRepository.findAllOrderedWithSources()).thenReturn(List.of());
        when(respondentSensorAssignmentRepository.findAllOrdered()).thenReturn(List.of());
        RespondentSensorAssignmentDto assignmentDto = new RespondentSensorAssignmentDto(
                null, respondentId, "respondent1", "manual", "Manual", null, null, null, true, 0);

        SurveySensorDataSettingsDto dto = service.updateAssignments(List.of(assignmentDto));

        assertThat(dto).isNotNull();
        verify(respondentSensorAssignmentRepository).deleteAll();
        verify(respondentSensorAssignmentRepository).saveAll(anyList());
        verify(surveySensorSettingsRepository, never()).save(any());
        verify(sensorTypeSettingRepository, never()).saveAll(any());
    }

    @Test
    void updateAssignments_rejectsAssignmentToAnUnknownSensorType() {
        UUID respondentId = UUID.randomUUID();
        IdentityUser respondent = new IdentityUser()
                .setId(respondentId)
                .setUsername("respondent1")
                .setRole("Respondent");
        when(sensorTypeRepository.findAll()).thenReturn(List.of());
        when(identityUserRepository.findById(respondentId)).thenReturn(Optional.of(respondent));
        RespondentSensorAssignmentDto assignmentDto = new RespondentSensorAssignmentDto(
                null, respondentId, "respondent1", "unknown_code", null, null, null, null, true, 0);

        assertThatThrownBy(() -> service.updateAssignments(List.of(assignmentDto)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown sensor type code");
    }

    @Test
    void uploadLogo_storesFileAndDeletesPreviousOne() throws Exception {
        SurveySettings existing = new SurveySettings(1, true, ",", ".", "/uploads/survey_settings/logo.png");
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.of(existing));
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MultipartFile file = new MockMultipartFile("file", "logo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(storageService.storeSurveySettingsLogo(file)).thenReturn("/uploads/survey_settings/logo.jpg");

        SurveySettingsDto dto = service.uploadLogo(file);

        assertThat(dto.logoPath()).isEqualTo("/uploads/survey_settings/logo.jpg");
        verify(storageService).deleteFile("/uploads/survey_settings/logo.png");
    }

    @Test
    void uploadLogo_keepsSameFileWithoutDeletingIt() throws Exception {
        SurveySettings existing = new SurveySettings(1, true, ",", ".", null);
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.of(existing));
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", new byte[]{1, 2, 3});
        when(storageService.storeSurveySettingsLogo(file)).thenReturn("/uploads/survey_settings/logo.png");

        service.uploadLogo(file);

        verify(storageService, never()).deleteFile(any());
    }

    @Test
    void deleteLogo_clearsPathAndDeletesFile() {
        SurveySettings existing = new SurveySettings(1, true, ",", ".", "/uploads/survey_settings/logo.png");
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.of(existing));
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SurveySettingsDto dto = service.deleteLogo();

        assertThat(dto.logoPath()).isNull();
        verify(storageService).deleteFile("/uploads/survey_settings/logo.png");
    }

    @Test
    void deleteLogo_noOpWhenNoLogoSet() {
        SurveySettings existing = new SurveySettings(1, true, ",", ".", null);
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.of(existing));
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SurveySettingsDto dto = service.deleteLogo();

        assertThat(dto.logoPath()).isNull();
        verify(storageService, never()).deleteFile(any());
    }

    @Test
    void getSensorDataSettings_createsDefaultNoSensorDataSettingsWhenMissing() {
        when(surveySensorSettingsRepository.findById(1)).thenReturn(Optional.empty());
        when(surveySensorSettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sensorTypeSettingRepository.findAllOrdered()).thenReturn(List.of());
        when(sensorParameterDefinitionRepository.findAllOrderedWithSources()).thenReturn(List.of());
        when(respondentSensorAssignmentRepository.findAllOrdered()).thenReturn(List.of());

        SurveySensorDataSettingsDto dto = service.getSensorDataSettings();

        assertThat(dto.mode()).isEqualTo("no_sensor_data");
        assertThat(dto.sensorTypes()).isEmpty();
        assertThat(dto.parameters()).isEmpty();
        assertThat(dto.assignments()).isEmpty();
        verify(surveySensorSettingsRepository).save(any(SurveySensorSettings.class));
    }
}
