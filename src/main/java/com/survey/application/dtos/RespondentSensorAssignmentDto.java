package com.survey.application.dtos;

import java.util.UUID;

public record RespondentSensorAssignmentDto(
        UUID id,
        UUID respondentId,
        String respondentUsername,
        String sensorTypeCode,
        String sensorTypeName,
        UUID sensorMacId,
        String sensorId,
        String sensorMac
) {}