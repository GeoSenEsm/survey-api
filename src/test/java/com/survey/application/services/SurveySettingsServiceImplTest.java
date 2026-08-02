package com.survey.application.services;

import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySettingsDto;
import com.survey.domain.models.SurveySettings;
import com.survey.domain.models.SurveySensorSettings;
import com.survey.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
                storageService);
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
