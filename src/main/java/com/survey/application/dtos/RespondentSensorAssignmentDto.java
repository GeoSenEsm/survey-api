package com.survey.application.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RespondentSensorAssignmentDto(
        UUID id,

        @NotNull
        UUID respondentId,

        String respondentUsername,

        @NotBlank
        @Size(max = 32)
        String sensorTypeCode,

        String sensorTypeName,
        UUID sensorMacId,
        String sensorId,
        String sensorMac,
        boolean enabled,

        @Min(0)
        int priorityOrder
) {}