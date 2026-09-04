package com.survey.application.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SensorTypeSettingDto(
        UUID id,

        @NotBlank
        @Size(max = 32)
        String sensorTypeCode,

        String sensorTypeName,
        String integrationMode,
        String adapterKey,
        boolean enabled,

        @Min(1)
        int connectionTimeoutSeconds,

        @Min(0)
        int displayOrder
) {}
