package com.survey.application.services;

import com.survey.application.dtos.statistics.DailyCompletionCompletedSlotDto;
import com.survey.application.dtos.statistics.DailyCompletionOverviewDto;
import com.survey.application.dtos.statistics.DailyCompletionRespondentDto;
import com.survey.application.dtos.statistics.DailyCompletionTimeSlotDto;
import com.survey.application.dtos.statistics.DailyStatsDetailDto;
import com.survey.application.dtos.statistics.GlobalStatsDetailDto;
import com.survey.application.dtos.statistics.GlobalStatsDto;
import com.survey.application.dtos.statistics.HourlySeriesPointDto;
import com.survey.application.dtos.statistics.ParticipantStatsDetailDto;
import com.survey.application.dtos.statistics.ParticipantStatsDto;
import com.survey.application.dtos.statistics.TimeSeriesPointDto;
import com.survey.api.security.Role;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SurveyParticipationTimeSlot;
import com.survey.domain.repository.IdentityUserRepository;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
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

    /**
     * Same tolerance the participation-time validation applies when
     * accepting a submission "just after" a slot's finish. We use it
     * here to decide whether a persisted {@code SurveyParticipation.date}
     * belongs to a given time slot.
     */
    private static final int SLOT_LATE_TOLERANCE_MINUTES = 5;

    /**
     * Length of the trailing "recently active" window used by the daily
     * detail view. A respondent is considered active for the selected
     * day if they submitted at least one survey during the previous
     * {@value} calendar days ending at the end of that day (inclusive).
     */
    private static final int DAILY_ACTIVE_WINDOW_DAYS = 3;

    private final SurveyParticipationRepository participationRepository;
    private final LocalizationDataRepository localizationDataRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SurveyParticipationTimeSlotRepository timeSlotRepository;
    private final IdentityUserRepository identityUserRepository;

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
        List<OffsetDateTime> outsideAreaDates =
                participationRepository.findDatesOutsideResearchAreaForRespondentInWindow(
                        respondentId,
                        stats.firstParticipationDate(),
                        stats.lastParticipationDate());

        LocalDate from = toUtcDate(stats.firstParticipationDate());
        LocalDate to = toUtcDate(stats.lastParticipationDate());

        return new ParticipantStatsDetailDto(
                stats,
                bucketByDay(participationDates, from, to),
                bucketByDay(locationDates, from, to),
                bucketByDay(sensorDates, from, to),
                bucketByDay(outsideAreaDates, from, to)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalStatsDetailDto getGlobalDetail() {
        List<ParticipantStatsDto> all = listParticipantStats();

        if (all.isEmpty()) {
            return new GlobalStatsDetailDto(
                    new GlobalStatsDto(null, null, 0, 0, 0, 0, 0, 0),
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
        long outsideResearchAreaCount = all.stream()
                .mapToLong(ParticipantStatsDto::outsideResearchAreaCount).sum();

        List<OffsetDateTime> participationDates = participationRepository.findAllDatesOrdered();
        List<OffsetDateTime> locationDates =
                localizationDataRepository.findAllDateTimesInWindow(firstDate, lastDate);
        List<OffsetDateTime> sensorDates =
                sensorDataRepository.findAllDateTimesInWindow(firstDate, lastDate);
        List<OffsetDateTime> outsideAreaDates =
                participationRepository.findDatesOutsideResearchAreaInWindow(firstDate, lastDate);

        LocalDate from = toUtcDate(firstDate);
        LocalDate to = toUtcDate(lastDate);

        GlobalStatsDto stats = new GlobalStatsDto(
                firstDate, lastDate,
                all.size(), surveysFilled, surveysAvailable,
                locationDataCount, sensorDataCount,
                outsideResearchAreaCount);

        return new GlobalStatsDetailDto(
                stats,
                bucketByDay(participationDates, from, to),
                bucketByDay(locationDates, from, to),
                bucketByDay(sensorDates, from, to),
                bucketByDay(outsideAreaDates, from, to)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DailyStatsDetailDto getDailyDetail(LocalDate date) {
        OffsetDateTime dayStart = date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<Object[]> participationTuples =
                participationRepository.findRespondentSurveyDateTuplesInWindow(dayStart, dayEnd);
        List<OffsetDateTime> locationDates =
                localizationDataRepository.findAllDateTimesInWindow(dayStart, dayEnd);
        List<OffsetDateTime> sensorDates =
                sensorDataRepository.findAllDateTimesInWindow(dayStart, dayEnd);
        List<OffsetDateTime> outsideAreaDates =
                participationRepository.findDatesOutsideResearchAreaInWindow(dayStart, dayEnd);

        long surveysFilled = participationTuples.size();
        // Available = one opportunity per (respondent account × time slot on the day).
        // Matches the natural "how many surveys could users have filled today" reading.
        long slotsThatDay = timeSlotRepository.countOverlappingWindow(dayStart, dayEnd);
        long surveysAvailable = (long) identityUserRepository.countRespondents() * slotsThatDay;

        Set<UUID> respondentsWithSubmission = new HashSet<>();
        List<OffsetDateTime> participationDates = new ArrayList<>(participationTuples.size());
        for (Object[] tuple : participationTuples) {
            respondentsWithSubmission.add((UUID) tuple[0]);
            participationDates.add((OffsetDateTime) tuple[2]);
        }

        OffsetDateTime activeWindowStart = dayEnd.minusDays(DAILY_ACTIVE_WINDOW_DAYS);
        Set<UUID> activeRespondentIds = new HashSet<>(
                participationRepository.findActiveRespondentIdsInWindow(activeWindowStart, dayEnd));
        long surveysFilledActive = participationTuples.stream()
                .filter(tuple -> activeRespondentIds.contains((UUID) tuple[0]))
                .count();
        long surveysAvailableActive = (long) activeRespondentIds.size() * slotsThatDay;

        return new DailyStatsDetailDto(
                date,
                respondentsWithSubmission.size(),
                surveysFilled,
                surveysAvailable,
                surveysFilledActive,
                surveysAvailableActive,
                activeRespondentIds.size(),
                DAILY_ACTIVE_WINDOW_DAYS,
                locationDates.size(),
                sensorDates.size(),
                outsideAreaDates.size(),
                bucketByHour(participationDates),
                bucketByHour(locationDates),
                bucketByHour(sensorDates),
                bucketByHour(outsideAreaDates)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DailyCompletionOverviewDto getDailyCompletion(LocalDate date) {
        OffsetDateTime dayStart = date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<SurveyParticipationTimeSlot> slots =
                timeSlotRepository.findOverlappingWindowWithSurvey(dayStart, dayEnd);

        List<DailyCompletionTimeSlotDto> timeSlotDtos = slots.stream()
                .map(slot -> new DailyCompletionTimeSlotDto(
                        slot.getId(),
                        slot.getSurveySendingPolicy().getSurvey().getId(),
                        slot.getSurveySendingPolicy().getSurvey().getName(),
                        slot.getStart(),
                        slot.getFinish()))
                .toList();

        List<IdentityUser> respondents = identityUserRepository.findByRole(Role.RESPONDENT.getRoleName());
        Map<UUID, Set<UUID>> completedByRespondent = findCompletedSlotsByRespondent(slots);
        Map<UUID, List<OffsetDateTime>> locationDatesByRespondent = findExtraDataDatesByRespondent(
                slots, localizationDataRepository::findRespondentDateTimesInWindow);
        Map<UUID, List<OffsetDateTime>> sensorDatesByRespondent = findExtraDataDatesByRespondent(
                slots, sensorDataRepository::findRespondentDateTimesInWindow);
        Map<UUID, OffsetDateTime> lastSubmissionByRespondent = findLastSubmissionByRespondent(dayEnd);

        List<DailyCompletionRespondentDto> respondentDtos = respondents.stream()
                .map(user -> toRespondentDto(
                        user,
                        completedByRespondent.getOrDefault(user.getId(), Set.of()),
                        slots,
                        locationDatesByRespondent.getOrDefault(user.getId(), List.of()),
                        sensorDatesByRespondent.getOrDefault(user.getId(), List.of()),
                        lastSubmissionByRespondent.get(user.getId())))
                .sorted(Comparator.comparingInt(DailyCompletionRespondentDto::completedCount).reversed()
                        .thenComparing(dto -> dto.username() == null ? "" : dto.username(),
                                String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new DailyCompletionOverviewDto(date, timeSlotDtos, respondentDtos);
    }

    private Map<UUID, Set<UUID>> findCompletedSlotsByRespondent(List<SurveyParticipationTimeSlot> slots) {
        if (slots.isEmpty()) {
            return Map.of();
        }

        // Widen the participation lookup by SLOT_LATE_TOLERANCE_MINUTES to
        // cover submissions accepted just after a slot's finish — otherwise
        // a legitimately late submission would look "missed".
        OffsetDateTime lookupFrom = slots.stream()
                .map(SurveyParticipationTimeSlot::getStart)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        OffsetDateTime lookupTo = slots.stream()
                .map(SurveyParticipationTimeSlot::getFinish)
                .max(Comparator.naturalOrder())
                .orElseThrow()
                .plusMinutes(SLOT_LATE_TOLERANCE_MINUTES);

        List<Object[]> tuples = participationRepository
                .findRespondentSurveyDateTuplesInWindow(lookupFrom, lookupTo);

        Map<UUID, List<SurveyParticipationTimeSlot>> slotsBySurvey = slots.stream()
                .collect(Collectors.groupingBy(slot ->
                        slot.getSurveySendingPolicy().getSurvey().getId()));

        Map<UUID, Set<UUID>> completedByRespondent = new HashMap<>();
        for (Object[] tuple : tuples) {
            UUID respondentId = (UUID) tuple[0];
            UUID surveyId = (UUID) tuple[1];
            OffsetDateTime participationDate = (OffsetDateTime) tuple[2];

            List<SurveyParticipationTimeSlot> candidates = slotsBySurvey.get(surveyId);
            if (candidates == null) continue;

            for (SurveyParticipationTimeSlot slot : candidates) {
                if (isParticipationInSlot(participationDate, slot)) {
                    completedByRespondent
                            .computeIfAbsent(respondentId, ignored -> new HashSet<>())
                            .add(slot.getId());
                    break;
                }
            }
        }
        return completedByRespondent;
    }

    /**
     * Groups every timestamp returned by {@code query(from, to)} for the
     * overall slot window under its respondent id, so we can later check
     * per-slot membership in O(n) without new DB round-trips. The tuples
     * are expected to be {@code [respondentId, OffsetDateTime]}.
     */
    private static Map<UUID, List<OffsetDateTime>> findExtraDataDatesByRespondent(
            List<SurveyParticipationTimeSlot> slots,
            java.util.function.BiFunction<OffsetDateTime, OffsetDateTime, List<Object[]>> query) {
        if (slots.isEmpty()) {
            return Map.of();
        }
        OffsetDateTime lookupFrom = slots.stream()
                .map(SurveyParticipationTimeSlot::getStart)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        OffsetDateTime lookupTo = slots.stream()
                .map(SurveyParticipationTimeSlot::getFinish)
                .max(Comparator.naturalOrder())
                .orElseThrow();

        Map<UUID, List<OffsetDateTime>> byRespondent = new HashMap<>();
        for (Object[] tuple : query.apply(lookupFrom, lookupTo)) {
            UUID respondentId = (UUID) tuple[0];
            OffsetDateTime moment = (OffsetDateTime) tuple[1];
            byRespondent.computeIfAbsent(respondentId, ignored -> new ArrayList<>()).add(moment);
        }
        return byRespondent;
    }

    private static boolean isParticipationInSlot(OffsetDateTime participationDate,
                                                 SurveyParticipationTimeSlot slot) {
        return !participationDate.isBefore(slot.getStart())
                && !participationDate.isAfter(slot.getFinish().plusMinutes(SLOT_LATE_TOLERANCE_MINUTES));
    }

    private static boolean anyTimestampInSlot(
            List<OffsetDateTime> timestamps, SurveyParticipationTimeSlot slot) {
        for (OffsetDateTime moment : timestamps) {
            if (!moment.isBefore(slot.getStart()) && !moment.isAfter(slot.getFinish())) {
                return true;
            }
        }
        return false;
    }

    private static DailyCompletionRespondentDto toRespondentDto(
            IdentityUser user,
            Set<UUID> completedSlotIds,
            List<SurveyParticipationTimeSlot> slots,
            List<OffsetDateTime> locationDates,
            List<OffsetDateTime> sensorDates,
            OffsetDateTime lastSubmissionAt) {
        List<DailyCompletionCompletedSlotDto> completedSlots = slots.stream()
                .filter(slot -> completedSlotIds.contains(slot.getId()))
                .sorted(Comparator.comparing(SurveyParticipationTimeSlot::getStart))
                .map(slot -> new DailyCompletionCompletedSlotDto(
                        slot.getId(),
                        anyTimestampInSlot(locationDates, slot),
                        anyTimestampInSlot(sensorDates, slot)))
                .toList();
        return new DailyCompletionRespondentDto(
                user.getId(), user.getUsername(),
                completedSlots, completedSlots.size(),
                lastSubmissionAt);
    }

    /**
     * Latest submission strictly before {@code cutoff} per respondent.
     * The daily-completion "active in last X days" filter feeds this the
     * end of the currently displayed calendar day so that a submission
     * made <em>after</em> that day does not push the respondent out of
     * the trailing window.
     */
    private Map<UUID, OffsetDateTime> findLastSubmissionByRespondent(OffsetDateTime cutoff) {
        Map<UUID, OffsetDateTime> byRespondent = new HashMap<>();
        for (Object[] tuple : participationRepository.findLastSubmissionDatePerRespondentBefore(cutoff)) {
            byRespondent.put((UUID) tuple[0], (OffsetDateTime) tuple[1]);
        }
        return byRespondent;
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
        long outsideResearchAreaCount =
                participationRepository.countOutsideResearchAreaByRespondentId(respondentId);
        return new ParticipantStatsDto(
                respondentId, username,
                firstDate, lastDate,
                surveysFilled, surveysAvailable,
                locationDataCount, sensorDataCount,
                outsideResearchAreaCount);
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

    /**
     * Groups {@code timestamps} by UTC hour-of-day and produces exactly
     * 24 points ({@code hour} = 0..23) with 0 for empty hours.
     */
    private static List<HourlySeriesPointDto> bucketByHour(List<OffsetDateTime> timestamps) {
        long[] counts = new long[24];
        for (OffsetDateTime moment : timestamps) {
            counts[moment.withOffsetSameInstant(ZoneOffset.UTC).getHour()]++;
        }
        List<HourlySeriesPointDto> series = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour++) {
            series.add(new HourlySeriesPointDto(hour, counts[hour]));
        }
        return series;
    }
}
