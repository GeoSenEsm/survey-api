package com.survey.application.services;

import java.util.List;

/**
 * A pre-built, install-on-demand BLE sensor definition bundled with the application. Templates
 * are code, not data: the catalog ships empty of installed sensor types, and an admin opts into
 * one via the Integrations page, which materializes it into a real {@code sensor_type} + published
 * {@code sensor_gatt_profile}.
 */
public record SensorProfileTemplate(
        String code,
        String name,
        String specJson,
        String minEngineVersion,
        List<ParameterMapping> parameters) {

    /**
     * Wires a used parameter as a source once the template is installed — reusing an existing
     * {@code sensor_parameter_definition} row with this code if one already exists (e.g. another
     * template installed first), or creating it from {@code name}/{@code dataType}/{@code unit}
     * otherwise. Nothing is pre-seeded by migration: a used parameter only starts existing once a
     * sensor type that actually produces it is installed.
     */
    public record ParameterMapping(String parameterCode, String name, String dataType, String unit) {
        public ParameterMapping(String parameterCode, String name, String dataType) {
            this(parameterCode, name, dataType, null);
        }
    }
}
