package com.survey.application.services;

import com.survey.application.dtos.SurveySettingsDto;
import com.survey.domain.models.SurveySettings;
import com.survey.domain.repository.SurveySettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveySettingsServiceImplTest {

    @Mock
    private SurveySettingsRepository surveySettingsRepository;

    private SurveySettingsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SurveySettingsServiceImpl(surveySettingsRepository);
    }

    @Test
    void getSettings_returnsExistingRow() {
        when(surveySettingsRepository.findById(1))
                .thenReturn(Optional.of(new SurveySettings(1, false, ";", ",")));

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
        SurveySettings existing = new SurveySettings(1, true, ",", ".");
        when(surveySettingsRepository.findById(1)).thenReturn(Optional.of(existing));
        when(surveySettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SurveySettingsDto dto = service.updateSettings(new SurveySettingsDto(false, ";", ","));

        assertThat(dto.showSendingPolicyCalendar()).isFalse();
        assertThat(dto.csvColumnSeparator()).isEqualTo(";");
        assertThat(dto.csvDecimalSeparator()).isEqualTo(",");
    }

    @Test
    void updateSettings_rejectsIdenticalSeparators() {
        assertThatThrownBy(() -> service.updateSettings(new SurveySettingsDto(true, ",", ",")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be different");
    }
}
