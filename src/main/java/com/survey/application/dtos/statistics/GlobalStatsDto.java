package com.survey.application.dtos.statistics;

import java.time.OffsetDateTime;

/**
 * Aggregated statistics across every participant that has ever filled
 * at least one survey. {@code firstParticipationDate} /
 * {@code lastParticipationDate} form the overall study window. Same
 * counting rules as {@link ParticipantStatsDto}.
 */
public record GlobalStatsDto(
        OffsetDateTime firstParticipationDate,
        OffsetDateTime lastParticipationDate,
        long totalParticipants,
        long surveysFilled,
        long surveysAvailable,
        long locationDataCount,
        long sensorDataCount,
        long outsideResearchAreaCount
) {}
