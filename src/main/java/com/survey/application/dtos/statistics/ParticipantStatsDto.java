package com.survey.application.dtos.statistics;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Summary statistics for a single participant over their personal study
 * window {@code [firstParticipationDate, lastParticipationDate]}.
 *
 * <p>The study window prefers the admin-assigned
 * {@code surveyStartDate}/{@code surveyEndDate} on {@code IdentityUser}
 * when both are set; otherwise it falls back to the first and last
 * submitted survey response. Any {@code SurveyParticipationTimeSlot}
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
