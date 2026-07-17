package com.survey.application.dtos.statistics;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Summary statistics for a single participant over their personal study
 * window {@code [firstParticipationDate, lastParticipationDate]}.
 *
 * <p>The domain rule that drives {@code surveysAvailable} is: <em>we
 * assume a participant took part in the study between their first and
 * last submitted survey response</em>. Any {@code SurveyParticipationTimeSlot}
 * (non-deleted) whose {@code [start, finish]} range overlaps that
 * window counts as "available" to the participant.</p>
 */
public record ParticipantStatsDto(
        UUID respondentId,
        String username,
        OffsetDateTime firstParticipationDate,
        OffsetDateTime lastParticipationDate,
        long surveysFilled,
        long surveysAvailable,
        long locationDataCount,
        long sensorDataCount,
        long outsideResearchAreaCount
) {}
