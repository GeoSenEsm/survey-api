package com.survey.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SurveySettingsDto(
        @NotNull Boolean showSendingPolicyCalendar,

        @NotBlank
        @Size(min = 1, max = 1)
        @Pattern(regexp = "[,;|\\t]", message = "csvColumnSeparator must be one of: , ; | or tab")
        String csvColumnSeparator,

        @NotBlank
        @Size(min = 1, max = 1)
        @Pattern(regexp = "[.,]", message = "csvDecimalSeparator must be '.' or ','")
        String csvDecimalSeparator,

        String logoPath
) {}
