package com.survey.application.dtos.statistics;

import java.time.LocalDate;
import java.util.List;

/**
 * Full-day survey completion overview for the admin "Daily completion"
 * page. Contains every non-deleted time slot whose window overlaps the
 * queried UTC day and, for every respondent account, the set of those
 * slots the respondent has already filled.
 */
public record DailyCompletionOverviewDto(
        LocalDate date,
        List<DailyCompletionTimeSlotDto> timeSlots,
        List<DailyCompletionRespondentDto> respondents
) {}
