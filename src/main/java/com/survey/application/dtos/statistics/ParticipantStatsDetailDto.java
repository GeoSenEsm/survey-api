package com.survey.application.dtos.statistics;

import java.util.List;

/** Detail response combining participant summary with daily series. */
public record ParticipantStatsDetailDto(
        ParticipantStatsDto stats,
        List<TimeSeriesPointDto> participationsPerDay,
        List<TimeSeriesPointDto> locationDataPerDay,
        List<TimeSeriesPointDto> sensorDataPerDay
) {}
