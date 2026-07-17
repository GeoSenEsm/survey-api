package com.survey.application.dtos.statistics;

import java.util.List;

/** Detail response combining global summary with daily series. */
public record GlobalStatsDetailDto(
        GlobalStatsDto stats,
        List<TimeSeriesPointDto> participationsPerDay,
        List<TimeSeriesPointDto> locationDataPerDay,
        List<TimeSeriesPointDto> sensorDataPerDay,
        /**
         * Daily count of survey participations that were submitted from
         * outside the configured research area (via the linked
         * {@code localization_data.outsideResearchArea} flag). Empty when
         * no research area polygon is defined.
         */
        List<TimeSeriesPointDto> participationsOutsideAreaPerDay
) {}
