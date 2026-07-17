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
 * is the timestamp of the respondent's most recent
 * {@code SurveyParticipation} that happened at or before the end of the
 * day currently displayed by the admin panel, or {@code null} if they
 * have never submitted anything before that day. Clipping to the
 * selected day (rather than reporting the all-time maximum) is what
 * lets the "active in last X days" filter behave correctly when the
 * admin browses past dates.
 */
public record DailyCompletionRespondentDto(
        UUID respondentId,
        String username,
        List<DailyCompletionCompletedSlotDto> completedSlots,
        int completedCount,
        OffsetDateTime lastSubmissionAt
) {}
