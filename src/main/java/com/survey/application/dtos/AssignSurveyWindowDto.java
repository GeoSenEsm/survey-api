package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Batch-assign (or clear) the study window for many respondents at once.
 * Passing {@code null} for both dates clears the window on every listed id.
 * Passing only one date is rejected by the service.
 */
public record AssignSurveyWindowDto(
        @NotEmpty List<@NotNull UUID> respondentIds,
        LocalDate surveyStartDate,
        LocalDate surveyEndDate
) {}
