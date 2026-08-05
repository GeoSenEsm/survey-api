package com.survey.application.services;

import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Shared {@code (name, unit)} uniqueness check for "used sensor data" parameters, mirroring
 * {@code UQ_sensor_parameter_definition_name_unit}. Used by both direct edits
 * ({@link SurveySettingsServiceImpl}) and promotion of a raw sensor type parameter
 * ({@link SensorTypeParameterServiceImpl}) so the two paths can't disagree. Rejects a genuine
 * conflict outright — no placeholder workaround, since every write here is a single row.
 */
@Component
public class SensorParameterDefinitionValidator {
    private final SensorParameterDefinitionRepository repository;

    public SensorParameterDefinitionValidator(SensorParameterDefinitionRepository repository) {
        this.repository = repository;
    }

    public void assertNameUnitAvailable(String name, String unit, UUID excludeId) {
        String normalizedName = normalize(name);
        String normalizedUnit = normalize(unit);
        boolean conflict = repository.findAll().stream()
                .filter(existing -> !existing.getId().equals(excludeId))
                .anyMatch(existing -> normalize(existing.getName()).equals(normalizedName)
                        && normalize(existing.getUnit()).equals(normalizedUnit));
        if (conflict) {
            throw new IllegalArgumentException(
                    "Parameter name and unit must be unique together: '" + name
                            + "' with unit '" + Objects.toString(unit, "") + "' is already used.");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
