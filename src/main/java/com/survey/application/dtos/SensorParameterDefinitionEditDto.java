package com.survey.application.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Write payload for {@code PUT /api/surveysettings/sensordata/parameters/{id}} — edits a single
 * "used sensor data" parameter. {@code code} is deliberately absent: it is the wire-format
 * identity referenced by stored sensor readings, GATT profile specs, and the mobile app, and is
 * immutable once created.
 */
public record SensorParameterDefinitionEditDto(
        @NotBlank
        @Size(max = 128)
        String name,

        @NotBlank
        @Pattern(regexp = "decimal|integer|boolean|text")
        String dataType,

        @Size(max = 32)
        String unit,

        @Min(0)
        int displayOrder
) {}
