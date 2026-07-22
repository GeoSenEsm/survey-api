package com.survey.application.dtos.statistics;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Per-respondent fulfillment metrics for the Issues tab.
 * Percentages are null when {@code surveysAvailable} is 0.
 */
public record RespondentIssueDto(
        UUID respondentId,
        String username,
        LocalDate windowStart,
        LocalDate windowEnd,
        long surveysFilled,
        long surveysAvailable,
        long gpsFilled,
        long sensorFilled,
        long skippedSurveys,
        Double surveyCompletionPercent,
        Double gpsCompletionPercent,
        Double sensorCompletionPercent
) {}
