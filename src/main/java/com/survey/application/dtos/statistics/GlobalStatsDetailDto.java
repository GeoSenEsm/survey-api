package com.survey.application.dtos.statistics;

import java.util.List;

/** Detail response combining global summary with daily series. */
public record GlobalStatsDetailDto(
        GlobalStatsDto stats,
        List<TimeSeriesPointDto> participationsPerDay,
        List<TimeSeriesPointDto> locationDataPerDay,
        List<TimeSeriesPointDto> sensorDataPerDay,
        /**
         * Top participants by surveys filled, for the "filled vs available"
         * comparison bar chart. Bounded server-side to keep payloads small.
         */
        List<ParticipantStatsDto> topParticipants
) {}
