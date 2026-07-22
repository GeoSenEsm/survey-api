package com.survey.application.services;

import com.survey.application.dtos.statistics.DailyCompletionOverviewDto;
import com.survey.application.dtos.statistics.DailyStatsDetailDto;
import com.survey.application.dtos.statistics.DailyStatsRowDto;
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
     * every respondent account, which of those slots were already filled
     * together with whether GPS and sensor data was also collected for
     * that slot.
     */
    DailyCompletionOverviewDto getDailyCompletion(LocalDate date);

    /**
     * Aggregates + hourly time series for one UTC calendar day. Same
     * shape as {@link #getGlobalDetail()} but with 24 hour buckets
     * instead of one bucket per study-window day.
     */
    DailyStatsDetailDto getDailyDetail(LocalDate date);

    /**
     * One KPI row per UTC day across the global study window (same
     * window as {@link #getGlobalDetail()}). Used for CSV export from
     * the daily statistics view.
     */
    List<DailyStatsRowDto> listDailyStatsRows();
}
