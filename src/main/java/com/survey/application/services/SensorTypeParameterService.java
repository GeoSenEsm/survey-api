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
     * Sets the fallback priority order of every raw source wired to one used parameter, in one
     * call — the admin picks the order (e.g. drag/up-down in the UI) and submits the full list.
     * {@code orderedSourceIds} must contain exactly the source ids currently linked to
     * {@code usedParameterId}, no more, no fewer.
     */
    List<SensorTypeParameterDto> reorderSources(UUID usedParameterId, List<UUID> orderedSourceIds);
}
