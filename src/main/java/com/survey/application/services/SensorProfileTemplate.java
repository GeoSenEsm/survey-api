package com.survey.application.services;

import java.util.List;

/**
 * A pre-built, install-on-demand BLE sensor definition bundled with the application. Templates
 * are code, not data: the catalog ships empty of installed sensor types (see
 * {@code V35__purge_seeded_sensor_profile_templates.sql}), and an admin opts into one via the
 * Integrations page, which materializes it into a real {@code sensor_type} + published
 * {@code sensor_gatt_profile}.
 */
public record SensorProfileTemplate(
        String code,
        String name,
        String specJson,
        String minEngineVersion,
        List<ParameterMapping> parameters) {

    /** Wires an existing {@code sensor_parameter_definition} as a source once the template is installed. */
    public record ParameterMapping(String parameterCode, int priorityOrder) {}
}
