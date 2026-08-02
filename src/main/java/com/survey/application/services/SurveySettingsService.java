package com.survey.application.services;

import com.survey.application.dtos.MobileSensorSetupDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySettingsDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SurveySettingsService {
    SurveySettingsDto getSettings();
    SurveySettingsDto updateSettings(SurveySettingsDto dto);
    SurveySettingsDto uploadLogo(MultipartFile file) throws IOException;
    SurveySettingsDto deleteLogo();
    SurveySensorDataSettingsDto getSensorDataSettings();
    SurveySensorDataSettingsDto updateSensorDataSettings(SurveySensorDataSettingsDto dto);
    MobileSensorSetupDto getMobileSensorSetup();
}
