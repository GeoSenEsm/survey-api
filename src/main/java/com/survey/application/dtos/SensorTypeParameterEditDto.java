package com.survey.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Write payload for {@code PUT /api/sensorprofiles/types/{sensorTypeId}/parameters/{id}}.
 * {@code code} is immutable once created — it is what a GATT profile spec's decoders reference
 * (see {@code SensorGattProfileServiceImpl.requireMappedParameters}); delete and recreate the row
 * if the raw code itself was wrong.
 */
public record SensorTypeParameterEditDto(
        @NotBlank
        @Size(max = 128)
        String name,

        @NotBlank
        @Pattern(regexp = "decimal|integer|boolean|text")
        String dataType,

        @Size(max = 32)
        String unit
) {}
