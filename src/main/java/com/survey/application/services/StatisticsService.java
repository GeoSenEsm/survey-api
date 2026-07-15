package com.survey.application.services;

import com.survey.application.dtos.statistics.DailyCompletionOverviewDto;
import com.survey.application.dtos.statistics.GlobalStatsDetailDto;
import com.survey.application.dtos.statistics.ParticipantStatsDetailDto;
import com.survey.application.dtos.statistics.ParticipantStatsDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Read-only aggregates for the admin "Statistics" tab. All queries are
 * scoped to respondents who have submitted at least one survey response.
 */
public interface StatisticsService {

    /** Summary row per respondent, sorted by surveys filled desc. */
    List<ParticipantStatsDto> listParticipantStats();

    /** Summary + daily time series for a single respondent. */
    ParticipantStatsDetailDto getParticipantDetail(UUID respondentId);

    /** Overall study aggregates + daily series across all respondents. */
    GlobalStatsDetailDto getGlobalDetail();

    /**
     * Full-day completion overview for {@code date} (interpreted as a UTC
     * calendar day). Lists every time slot that overlaps the day and, for
     * every respondent account, which of those slots were already filled.
     */
    DailyCompletionOverviewDto getDailyCompletion(LocalDate date);
}
