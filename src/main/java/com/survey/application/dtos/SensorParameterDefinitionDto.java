package com.survey.application.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SensorParameterDefinitionDto(
        UUID id,

        @NotBlank
        @Size(max = 64)
        String code,

        @NotBlank
        @Size(max = 128)
        String name,

        @NotBlank
        @Pattern(regexp = "decimal|integer|boolean|text")
        String dataType,

        @Size(max = 32)
        String unit,

        boolean required,
        boolean active,

        @Min(0)
        int displayOrder,

        @NotNull
        List<@Valid SensorParameterSourceDto> sources
) {}
