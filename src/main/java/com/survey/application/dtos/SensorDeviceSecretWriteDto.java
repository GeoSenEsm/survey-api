package com.survey.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SensorDeviceSecretWriteDto(
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank
        @Pattern(regexp = "[0-9A-Fa-f]{32}", message = "must be a 16-byte hexadecimal value")
        String value
) {}
