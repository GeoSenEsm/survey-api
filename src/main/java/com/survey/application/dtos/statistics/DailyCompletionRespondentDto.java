package com.survey.application.dtos.statistics;

import java.util.List;
import java.util.UUID;

/**
 * Per-respondent view of the day: which time slots the respondent has
 * already filled and the total count. {@code completedTimeSlotIds}
 * references entries in
 * {@link DailyCompletionOverviewDto#timeSlots()}.
 */
public record DailyCompletionRespondentDto(
        UUID respondentId,
        String username,
        List<UUID> completedTimeSlotIds,
        int completedCount
) {}
