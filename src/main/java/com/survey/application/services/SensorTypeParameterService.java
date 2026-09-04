package com.survey.application.services;

import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SensorTypeParameterEditDto;
import com.survey.application.dtos.UseSensorTypeParameterDto;

import java.util.List;
import java.util.UUID;

/**
 * Manages a sensor type's own raw parameter catalog — what that sensor type can possibly
 * produce, independent of whether any of it has been promoted ("used") into the globally-unique
 * "used sensor data" list ({@code sensor_parameter_definition}, see {@link SurveySettingsService}).
 */
public interface SensorTypeParameterService {
    List<SensorTypeParameterDto> list(UUID sensorTypeId);

    SensorTypeParameterDto create(UUID sensorTypeId, SensorTypeParameterCreateDto dto);

    SensorTypeParameterDto update(UUID sensorTypeId, UUID id, SensorTypeParameterEditDto dto);

    void delete(UUID sensorTypeId, UUID id);

    SensorTypeParameterDto use(UUID sensorTypeId, UUID id, UseSensorTypeParameterDto dto);

    SensorTypeParameterDto unuse(UUID sensorTypeId, UUID id);

    /**
     * Guarantees a used parameter always has {@code manual} wired as a fallback source, so a
     * respondent can always be prompted to enter it by hand. Idempotent: a no-op if the source
     * already exists. Called from every path that creates a
     * {@code sensor_parameter_definition} row, so this is never left to a best-effort client call.
     */
    void ensureManualSource(UUID usedParameterId);
}
