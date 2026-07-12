package com.survey.application.services;

import com.survey.application.dtos.statistics.GlobalStatsDetailDto;
import com.survey.application.dtos.statistics.GlobalStatsDto;
import com.survey.application.dtos.statistics.ParticipantStatsDetailDto;
import com.survey.application.dtos.statistics.ParticipantStatsDto;
import com.survey.application.dtos.statistics.TimeSeriesPointDto;
import com.survey.domain.repository.LocalizationDataRepository;
import com.survey.domain.repository.SensorDataRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveyParticipationTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Computes global and per-participant study aggregates.
 *
 * <p>Design notes:
 * <ul>
 *   <li>Everything is bucketed by UTC calendar day — we don't try to
 *       auto-detect the researcher's timezone. This keeps series aligned
 *       with the ISO-8601 timestamps we already persist.</li>
 *   <li>"Study window" for a participant is
 *       {@code [firstParticipationDate, lastParticipationDate]}. If they
 *       filled only one survey both dates are equal and the window
 *       collapses to a single instant.</li>
 *   <li>Global "surveys available" is the <em>sum</em> of every
 *       participant's own {@code surveysAvailable} (an "opportunity" is
 *       always evaluated against a single participant's window). This
 *       keeps {@code surveysFilled / surveysAvailable} a valid ratio at
 *       both scopes and prevents time slots outside all study windows
 *       from being counted at all.</li>
 *   <li>Aggregation is done in Java rather than SQL {@code DATE(x)} so
 *       we stay portable across SQL Server versions. Datasets are
 *       expected to be at most tens of thousands of rows per study; if
 *       that ever changes, promote the grouping to SQL.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final int TOP_PARTICIPANTS_LIMIT = 20;

    private final SurveyParticipationRepository participationRepository;
    private final LocalizationDataRepository localizationDataRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SurveyParticipationTimeSlotRepository timeSlotRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantStatsDto> listParticipantStats() {
        return participationRepository.aggregateParticipationsPerRespondent().stream()
                .map(this::toParticipantStats)
                .sorted(Comparator.comparingLong(ParticipantStatsDto::surveysFilled).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipantStatsDetailDto getParticipantDetail(UUID respondentId) {
        ParticipantStatsDto stats = listParticipantStats().stream()
                .filter(p -> p.respondentId().equals(respondentId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No participation found for respondent " + respondentId));

        List<OffsetDateTime> participationDates =
                participationRepository.findDatesByRespondentId(respondentId);
        List<OffsetDateTime> locationDates =
                localizationDataRepository.findDateTimesForRespondentInWindow(
                        respondentId,
                        stats.firstParticipationDate(),
                        stats.lastParticipationDate());
        List<OffsetDateTime> sensorDates =
                sensorDataRepository.findDateTimesForRespondentInWindow(
                        respondentId,
                        stats.firstParticipationDate(),
                        stats.lastParticipationDate());

        LocalDate from = toUtcDate(stats.firstParticipationDate());
        LocalDate to = toUtcDate(stats.lastParticipationDate());

        return new ParticipantStatsDetailDto(
                stats,
                bucketByDay(participationDates, from, to),
                bucketByDay(locationDates, from, to),
                bucketByDay(sensorDates, from, to)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalStatsDetailDto getGlobalDetail() {
        List<ParticipantStatsDto> all = listParticipantStats();

        if (all.isEmpty()) {
            return new GlobalStatsDetailDto(
                    new GlobalStatsDto(null, null, 0, 0, 0, 0, 0),
                    List.of(), List.of(), List.of(), List.of());
        }

        OffsetDateTime firstDate = all.stream()
                .map(ParticipantStatsDto::firstParticipationDate)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        OffsetDateTime lastDate = all.stream()
                .map(ParticipantStatsDto::lastParticipationDate)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        long surveysFilled = all.stream().mapToLong(ParticipantStatsDto::surveysFilled).sum();
        // Sum per-participant availabilities rather than counting distinct
        // slots over the union window: a time slot that overlaps N
        // participants' study windows represents N opportunities, and one
        // that falls in a gap between participants represents zero. This
        // keeps `surveysFilled / surveysAvailable` a valid ratio.
        long surveysAvailable = all.stream().mapToLong(ParticipantStatsDto::surveysAvailable).sum();
        long locationDataCount = all.stream().mapToLong(ParticipantStatsDto::locationDataCount).sum();
        long sensorDataCount = all.stream().mapToLong(ParticipantStatsDto::sensorDataCount).sum();

        List<OffsetDateTime> participationDates = participationRepository.findAllDatesOrdered();
        List<OffsetDateTime> locationDates =
                localizationDataRepository.findAllDateTimesInWindow(firstDate, lastDate);
        List<OffsetDateTime> sensorDates =
                sensorDataRepository.findAllDateTimesInWindow(firstDate, lastDate);

        LocalDate from = toUtcDate(firstDate);
        LocalDate to = toUtcDate(lastDate);

        GlobalStatsDto stats = new GlobalStatsDto(
                firstDate, lastDate,
                all.size(), surveysFilled, surveysAvailable,
                locationDataCount, sensorDataCount);

        List<ParticipantStatsDto> topParticipants = all.stream()
                .limit(TOP_PARTICIPANTS_LIMIT)
                .toList();

        return new GlobalStatsDetailDto(
                stats,
                bucketByDay(participationDates, from, to),
                bucketByDay(locationDates, from, to),
                bucketByDay(sensorDates, from, to),
                topParticipants
        );
    }

    private ParticipantStatsDto toParticipantStats(Object[] row) {
        UUID respondentId = (UUID) row[0];
        String username = (String) row[1];
        OffsetDateTime firstDate = (OffsetDateTime) row[2];
        OffsetDateTime lastDate = (OffsetDateTime) row[3];
        long surveysFilled = ((Number) row[4]).longValue();
        long surveysAvailable = timeSlotRepository.countOverlappingWindow(firstDate, lastDate);
        long locationDataCount = localizationDataRepository.countByIdentityUserId(respondentId);
        long sensorDataCount = sensorDataRepository.countByRespondentId(respondentId);
        return new ParticipantStatsDto(
                respondentId, username,
                firstDate, lastDate,
                surveysFilled, surveysAvailable,
                locationDataCount, sensorDataCount);
    }

    private static LocalDate toUtcDate(OffsetDateTime moment) {
        return moment.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
    }

    /**
     * Groups {@code timestamps} by UTC day and fills every calendar day
     * from {@code from} to {@code to} inclusive (with 0 for empty days),
     * so echarts renders a continuous x-axis instead of a jagged one.
     */
    private static List<TimeSeriesPointDto> bucketByDay(
            List<OffsetDateTime> timestamps, LocalDate from, LocalDate to) {

        Map<LocalDate, Long> counts = timestamps.stream()
                .collect(Collectors.groupingBy(
                        StatisticsServiceImpl::toUtcDate,
                        TreeMap::new,
                        Collectors.counting()));

        List<TimeSeriesPointDto> series = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            series.add(new TimeSeriesPointDto(day, counts.getOrDefault(day, 0L)));
        }
        return series;
    }
}
