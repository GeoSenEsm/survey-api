package com.survey.application.dtos.statistics;

import java.time.LocalDate;

/**
 * KPI numbers for one UTC calendar day — the same figures shown in the
 * daily-statistics boxes, without hourly chart series. Used for CSV
 * export across the whole study window (one row per day).
 */
public record DailyStatsRowDto(
        LocalDate date,
        long totalParticipants,
        long surveysFilled,
        long surveysAvailable,
        long surveysFilledActive,
        long surveysAvailableActive,
        int activeRespondentCount,
        long locationDataCount,
        long sensorDataCount,
        long participationsOutsideAreaCount
) {}
