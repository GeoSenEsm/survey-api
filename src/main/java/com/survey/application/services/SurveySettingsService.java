package com.survey.application.services;

import com.survey.application.dtos.SurveySettingsDto;

public interface SurveySettingsService {
    SurveySettingsDto getSettings();
    SurveySettingsDto updateSettings(SurveySettingsDto dto);
}
