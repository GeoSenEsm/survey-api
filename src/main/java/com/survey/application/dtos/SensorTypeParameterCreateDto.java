package com.survey.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Write payload for {@code POST /api/sensorprofiles/types/{sensorTypeId}/parameters} — declares
 * a raw parameter that sensor type can produce. {@code code} must match whatever raw field name
 * the device/profile spec actually emits (e.g. a GATT profile decoder's {@code "parameter"}
 * value); it is not required to be globally unique, only unique within this sensor type.
 */
public record SensorTypeParameterCreateDto(
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
        String unit
) {}
