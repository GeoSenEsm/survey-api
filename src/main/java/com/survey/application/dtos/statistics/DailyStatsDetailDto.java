package com.survey.application.dtos.statistics;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregate stats + hourly time series for a single UTC calendar day.
 * {@code surveysAvailable} counts opportunities for respondents with no
 * assigned window or whose window covers {@code date}.
 * {@code surveysAvailableActive} / {@code activeRespondentCount} restrict
 * that set to respondents that have both dates assigned and whose window
 * covers {@code date} ("Available (set dates)" in the admin UI).
 */
public record DailyStatsDetailDto(
        LocalDate date,
        long totalParticipants,
        long surveysFilled,
        long surveysAvailable,
        long surveysFilledActive,
        long surveysAvailableActive,
        int activeRespondentCount,
        long locationDataCount,
        long sensorDataCount,
        long participationsOutsideAreaCount,
        List<HourlySeriesPointDto> participationsPerHour,
        List<HourlySeriesPointDto> locationDataPerHour,
        List<HourlySeriesPointDto> sensorDataPerHour,
        List<HourlySeriesPointDto> participationsOutsideAreaPerHour
) {}
