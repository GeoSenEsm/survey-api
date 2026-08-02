package com.survey.application.services;

import com.survey.application.dtos.SurveySettingsDto;
import com.survey.domain.models.SurveySettings;
import com.survey.domain.repository.SurveySettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SurveySettingsServiceImpl implements SurveySettingsService {
    private static final int SINGLETON_ID = 1;
    private static final String DEFAULT_COLUMN_SEPARATOR = ",";
    private static final String DEFAULT_DECIMAL_SEPARATOR = ".";

    private final SurveySettingsRepository surveySettingsRepository;

    public SurveySettingsServiceImpl(SurveySettingsRepository surveySettingsRepository) {
        this.surveySettingsRepository = surveySettingsRepository;
    }

    @Override
    public SurveySettingsDto getSettings() {
        return toDto(getOrCreate());
    }

    @Override
    public SurveySettingsDto updateSettings(SurveySettingsDto dto) {
        if (dto.csvColumnSeparator().equals(dto.csvDecimalSeparator())) {
            throw new IllegalArgumentException(
                    "csvColumnSeparator and csvDecimalSeparator must be different.");
        }

        SurveySettings settings = getOrCreate();
        settings.setShowSendingPolicyCalendar(dto.showSendingPolicyCalendar());
        settings.setCsvColumnSeparator(dto.csvColumnSeparator());
        settings.setCsvDecimalSeparator(dto.csvDecimalSeparator());
        return toDto(surveySettingsRepository.save(settings));
    }

    private SurveySettings getOrCreate() {
        return surveySettingsRepository.findById(SINGLETON_ID)
                .orElseGet(() -> surveySettingsRepository.save(
                        new SurveySettings(
                                SINGLETON_ID,
                                true,
                                DEFAULT_COLUMN_SEPARATOR,
                                DEFAULT_DECIMAL_SEPARATOR)));
    }

    private static SurveySettingsDto toDto(SurveySettings settings) {
        return new SurveySettingsDto(
                settings.isShowSendingPolicyCalendar(),
                settings.getCsvColumnSeparator(),
                settings.getCsvDecimalSeparator());
    }
}
