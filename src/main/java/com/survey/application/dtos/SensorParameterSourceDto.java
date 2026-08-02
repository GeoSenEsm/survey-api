package com.survey.application.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SensorParameterSourceDto(
        UUID id,

        @NotBlank
        @Size(max = 32)
        String sensorTypeCode,

        @Min(0)
        int priorityOrder
) {}
