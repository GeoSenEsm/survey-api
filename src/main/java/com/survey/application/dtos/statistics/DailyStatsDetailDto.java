package com.survey.application.dtos.statistics;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregate stats + hourly time series for a single UTC calendar day.
 * Same shape as {@link GlobalStatsDetailDto} but bucketed by hour of
 * that day rather than by day of the whole study window.
 */
public record DailyStatsDetailDto(
        LocalDate date,
        long totalParticipants,
        long surveysFilled,
        long surveysAvailable,
        /**
         * Filled/available restricted to "active" respondents — those
         * who submitted at least one survey inside the trailing window
         * ending at the end of {@code date} (see
         * {@link com.survey.application.services.StatisticsServiceImpl}
         * for the exact window length).
         */
        long surveysFilledActive,
        long surveysAvailableActive,
        int activeRespondentCount,
        int activeWindowDays,
        long locationDataCount,
        long sensorDataCount,
        long participationsOutsideAreaCount,
        List<HourlySeriesPointDto> participationsPerHour,
        List<HourlySeriesPointDto> locationDataPerHour,
        List<HourlySeriesPointDto> sensorDataPerHour,
        List<HourlySeriesPointDto> participationsOutsideAreaPerHour
) {}
