package com.survey.application.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SensorGattProfileWriteDto(
        @NotNull JsonNode spec,

        @NotBlank
        @Pattern(regexp = "\\d+\\.\\d+\\.\\d+", message = "must be a semantic version such as 1.0.0")
        String minEngineVersion
) {}
