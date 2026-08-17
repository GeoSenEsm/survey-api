package com.survey.application.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * Write payload for {@code PUT /api/surveysettings/sensordata}. Which physical sensor a
 * respondent has is managed separately, always-unlocked, via
 * {@code PUT /api/sensormac/{sensorId}/respondent} (Sensor devices screen) — an operational fact
 * that keeps changing throughout a live study (respondents join, devices get swapped), unlike the
 * mode and sensor type catalog here, which are frozen once the initial survey is published.
 * Parameter definitions are also excluded: they are managed one at a time via
 * {@code POST}/{@code PUT /api/surveysettings/sensordata/parameters[/{id}]} and via a sensor
 * type's raw parameter catalog ({@code /api/sensorprofiles/types/{sensorTypeId}/parameters}), not
 * as a bulk replace.
 */
public record SurveySensorDataSettingsWriteDto(
        @NotBlank
        @Pattern(regexp = "no_sensor_data|configured_sensors")
        String mode,

        @NotNull
        List<@Valid SensorTypeSettingDto> sensorTypes
) {}
