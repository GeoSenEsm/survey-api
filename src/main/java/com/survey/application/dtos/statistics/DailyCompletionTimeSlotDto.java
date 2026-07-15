package com.survey.application.dtos.statistics;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One time slot that overlaps the queried day. Callers use
 * {@code finish} together with the current time to distinguish
 * "missed" from "pending" for respondents that did not fill it.
 */
public record DailyCompletionTimeSlotDto(
        UUID id,
        UUID surveyId,
        String surveyName,
        OffsetDateTime start,
        OffsetDateTime finish
) {}
