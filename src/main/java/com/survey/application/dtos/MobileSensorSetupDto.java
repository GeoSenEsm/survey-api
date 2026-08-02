package com.survey.application.dtos;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record MobileSensorSetupDto(
        String mode,
        List<SensorTypeSettingDto> sensorTypes,
        List<SensorParameterDefinitionDto> parameters,
        List<RespondentSensorAssignmentDto> assignments,
        List<JsonNode> gattProfiles,
        List<MobileSensorDeviceSecretsDto> deviceSecrets
) {}
