package com.survey.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for setting a device secret. Never returned by any endpoint, so there is no need
 * to mark {@code value} write-only for Jackson: doing so previously made Jackson drop the field
 * whenever this record itself was serialized (e.g. by a Java HTTP client), not just when it was
 * embedded in a response.
 */
public record SensorDeviceSecretWriteDto(
        @NotBlank
        @Pattern(regexp = "[0-9A-Fa-f]{32}", message = "must be a 16-byte hexadecimal value")
        String value
) {}
