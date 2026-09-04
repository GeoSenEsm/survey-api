package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A pre-built BLE sensor definition the admin can install from the Integrations page.")
public record SensorProfileTemplateDto(
        @Schema(description = "Stable key; becomes the sensor_type code once installed.") String code,
        @Schema(description = "Admin-facing display name.") String name,
        @Schema(description = "Codes of the parameters this sensor provides once installed.") List<String> parameterCodes,
        @Schema(description = "True when a sensor type with this code already exists.") boolean installed) {
}
