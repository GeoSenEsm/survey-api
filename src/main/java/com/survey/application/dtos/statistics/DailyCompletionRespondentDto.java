package com.survey.application.dtos.statistics;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Per-respondent view of the day: which time slots the respondent has
 * already filled together with, for every filled slot, whether GPS and
 * sensor data was also collected during that slot's window.
 * {@code completedSlots} references entries in
 * {@link DailyCompletionOverviewDto#timeSlots()}. {@code lastSubmissionAt}
 * is the timestamp of the respondent's most recent {@code SurveyParticipation}
 * across the entire study (not just this day) or {@code null} if they have
 * never submitted anything — used by the admin panel to filter out
 * inactive accounts.
 */
public record DailyCompletionRespondentDto(
        UUID respondentId,
        String username,
        List<DailyCompletionCompletedSlotDto> completedSlots,
        int completedCount,
        OffsetDateTime lastSubmissionAt
) {}
