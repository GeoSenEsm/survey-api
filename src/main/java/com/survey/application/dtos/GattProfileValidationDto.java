package com.survey.application.dtos;

import java.util.List;
import java.util.Map;

public record GattProfileValidationDto(
        boolean valid,
        String canonicalHash,
        List<String> errors,
        List<GoldenVectorResultDto> goldenVectors
) {
    public GattProfileValidationDto(boolean valid, String canonicalHash, List<String> errors) {
        this(valid, canonicalHash, errors, List.of());
    }

    public record GoldenVectorResultDto(
            String name,
            boolean passed,
            List<String> errors,
            Map<String, Double> decodedValues
    ) {}
}
