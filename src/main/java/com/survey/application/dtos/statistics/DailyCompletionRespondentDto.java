package com.survey.application.dtos.statistics;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Per-respondent view of the day: which time slots the respondent has
 * already filled together with, for every filled slot, whether GPS and
 * sensor data was also collected during that slot's window.
 * {@code completedSlots} references entries in
 * {@link DailyCompletionOverviewDto#timeSlots()}.
 * {@code surveyStartDate}/{@code surveyEndDate} are the admin-assigned
 * study window (both null when unset) and drive the "only respondents
 * with set dates" filter on the daily-completion page.
 */
public record DailyCompletionRespondentDto(
        UUID respondentId,
        String username,
        List<DailyCompletionCompletedSlotDto> completedSlots,
        int completedCount,
        LocalDate surveyStartDate,
        LocalDate surveyEndDate
) {}
