package com.survey.application.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Write payload for {@code POST /api/sensorprofiles/types/{sensorTypeId}/parameters/{id}/use} —
 * promotes a raw catalog row into "used sensor data".
 * <p>
 * Exactly one of two shapes is valid, enforced in {@code SensorTypeParameterServiceImpl.use}
 * (not via bean validation, since it's a cross-field either/or):
 * <ul>
 *     <li>{@code usedParameterId} set — link this raw row to that already-used parameter as an
 *     additional fallback source (preserves multi-sensor-type priority chains).</li>
 *     <li>{@code usedParameterId} null — create a new used parameter from {@code name}/{@code
 *     unit}/{@code dataType}/{@code required} (defaulting {@code code} to the raw row's own
 *     code) and link to it.</li>
 * </ul>
 */
public record UseSensorTypeParameterDto(
        UUID usedParameterId,

        @Size(max = 128)
        String name,

        @Pattern(regexp = "decimal|integer|boolean|text")
        String dataType,

        @Size(max = 32)
        String unit,

        boolean required
) {}
