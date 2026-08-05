package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Write payload for {@code PUT /api/surveysettings/sensordata/parameters/{id}/sources} — sets
 * the fallback priority order of a used parameter's raw sources in one call.
 */
public record ReorderSensorParameterSourcesDto(
        @NotEmpty
        List<UUID> sourceIds
) {}
