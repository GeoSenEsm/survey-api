package com.survey.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Write payload for {@code POST /api/surveysettings/sensordata/parameters} — creates a "used
 * sensor data" parameter directly, without going through a sensor type's raw parameter catalog.
 * This is how manual-only parameters (no physical sensor behind them) get created.
 */
public record SensorParameterDefinitionCreateDto(
        @NotBlank
        @Size(max = 64)
        String code,

        @NotBlank
        @Size(max = 128)
        String name,

        @NotBlank
        @Pattern(regexp = "decimal|integer|boolean|text")
        String dataType,

        @Size(max = 32)
        String unit,

        boolean required
) {}
