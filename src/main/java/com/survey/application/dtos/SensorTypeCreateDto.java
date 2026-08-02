package com.survey.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SensorTypeCreateDto(
        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "[a-z][a-z0-9_-]*")
        String code,

        @NotBlank
        @Size(max = 64)
        String name,

        @NotBlank
        @Pattern(regexp = "profile|native|manual|none")
        String integrationMode,

        @Size(max = 64)
        @Pattern(regexp = "[A-Za-z0-9._-]*")
        String adapterKey
) {}
