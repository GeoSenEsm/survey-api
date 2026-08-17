package com.survey.application.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record SurveySensorDataSettingsDto(
        @NotBlank
        @Pattern(regexp = "no_sensor_data|configured_sensors")
        String mode,

        @NotNull
        List<@Valid SensorTypeSettingDto> sensorTypes,

        @NotNull
        List<@Valid SensorParameterDefinitionDto> parameters
) {}
