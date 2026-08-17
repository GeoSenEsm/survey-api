package com.survey.application.dtos;

import java.util.UUID;

/**
 * Read shape for one row of a sensor type's raw parameter catalog
 * ({@code GET /api/sensorprofiles/types/{sensorTypeId}/parameters}).
 */
public record SensorTypeParameterDto(
        UUID id,
        UUID sensorTypeId,
        String sensorTypeCode,
        String code,
        String name,
        String dataType,
        String unit,
        UUID usedParameterId,
        String usedParameterCode
) {}
