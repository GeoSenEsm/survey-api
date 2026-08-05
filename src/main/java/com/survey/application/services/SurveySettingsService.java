package com.survey.application.services;

import com.survey.application.dtos.MobileSensorSetupDto;
import com.survey.application.dtos.RespondentSensorAssignmentDto;
import com.survey.application.dtos.SensorParameterDefinitionCreateDto;
import com.survey.application.dtos.SensorParameterDefinitionDto;
import com.survey.application.dtos.SensorParameterDefinitionEditDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.SurveySettingsDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface SurveySettingsService {
    SurveySettingsDto getSettings();
    SurveySettingsDto updateSettings(SurveySettingsDto dto);
    SurveySettingsDto uploadLogo(MultipartFile file) throws IOException;
    SurveySettingsDto deleteLogo();
    SurveySensorDataSettingsDto getSensorDataSettings();
    SurveySensorDataSettingsDto updateSensorDataSettings(SurveySensorDataSettingsWriteDto dto);
    SurveySensorDataSettingsDto updateAssignments(List<RespondentSensorAssignmentDto> assignments);
    MobileSensorSetupDto getMobileSensorSetup();
    SensorParameterDefinitionDto createSensorParameterDefinition(SensorParameterDefinitionCreateDto dto);
    SensorParameterDefinitionDto updateSensorParameterDefinition(UUID id, SensorParameterDefinitionEditDto dto);
}
