package com.survey.application.services;

import com.survey.application.dtos.statistics.DailyCompletionCompletedSlotDto;
import com.survey.application.dtos.statistics.DailyCompletionOverviewDto;
import com.survey.application.dtos.statistics.DailyCompletionRespondentDto;
import com.survey.application.dtos.statistics.DailyCompletionTimeSlotDto;
import com.survey.application.dtos.statistics.DailyStatsDetailDto;
import com.survey.application.dtos.statistics.DailyStatsRowDto;
import com.survey.application.dtos.statistics.GlobalStatsDetailDto;
import com.survey.application.dtos.statistics.GlobalStatsDto;
import com.survey.application.dtos.statistics.HourlySeriesPointDto;
import com.survey.application.dtos.statistics.IssuesOverviewDto;
import com.survey.application.dtos.statistics.ParticipantStatsDetailDto;
import com.survey.application.dtos.statistics.ParticipantStatsDto;
import com.survey.application.dtos.statistics.RespondentIssueDto;
import com.survey.application.dtos.statistics.TimeSeriesPointDto;
import com.survey.api.security.Role;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SurveyParticipationTimeSlot;
import com.survey.domain.models.enums.IssuesRangeMode;
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
 *   <li>"Study window" for a participant prefers the admin-assigned
 *       survey start/end dates on {@code IdentityUser} when both are set;
 *       otherwise it falls back to
 *       {@code [firstParticipationDate, lastParticipationDate]}.</li>
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

    private final SurveyParticipationRepository participationRepository;
    private final LocalizationDataRepository localizationDataRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SurveyParticipationTimeSlotRepository timeSlotRepository;
    private final IdentityUserRepository identityUserRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantStatsDto> listParticipantStats() {
        Map<UUID, IdentityUser> respondentsById = identityUserRepository
                .findByRole(Role.RESPONDENT.getRoleName()).stream()
                .collect(Collectors.toMap(IdentityUser::getId, u -> u));
        return participationRepository.aggregateParticipationsPerRespondent().stream()
                .map(row -> toParticipantStats(row, respondentsById.get((UUID) row[0])))
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
        long slotsThatDay = timeSlotRepository.countOverlappingWindow(dayStart, dayEnd);
        long availableRespondents = identityUserRepository.countRespondentsAvailableOn(date);
        long surveysAvailable = availableRespondents * slotsThatDay;

        Set<UUID> respondentsWithSubmission = new HashSet<>();
        List<OffsetDateTime> participationDates = new ArrayList<>(participationTuples.size());
        for (Object[] tuple : participationTuples) {
            respondentsWithSubmission.add((UUID) tuple[0]);
            participationDates.add((OffsetDateTime) tuple[2]);
        }

        Set<UUID> setDatesRespondentIds = new HashSet<>(
                identityUserRepository.findRespondentIdsWithWindowCovering(date));
        long surveysFilledActive = participationTuples.stream()
                .filter(tuple -> setDatesRespondentIds.contains((UUID) tuple[0]))
                .count();
        long surveysAvailableActive = (long) setDatesRespondentIds.size() * slotsThatDay;

        return new DailyStatsDetailDto(
                date,
                respondentsWithSubmission.size(),
                surveysFilled,
                surveysAvailable,
                surveysFilledActive,
                surveysAvailableActive,
                setDatesRespondentIds.size(),
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
    public List<DailyStatsRowDto> listDailyStatsRows() {
        List<ParticipantStatsDto> participants = listParticipantStats();
        if (participants.isEmpty()) {
            return List.of();
        }

        OffsetDateTime windowStart = participants.stream()
                .map(ParticipantStatsDto::firstParticipationDate)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        OffsetDateTime windowEnd = participants.stream()
                .map(ParticipantStatsDto::lastParticipationDate)
                .max(Comparator.naturalOrder())
                .orElseThrow();

        LocalDate from = toUtcDate(windowStart);
        LocalDate to = toUtcDate(windowEnd);
        OffsetDateTime rangeStart = from.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime rangeEndExclusive = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

        List<IdentityUser> respondents =
                identityUserRepository.findByRole(Role.RESPONDENT.getRoleName());
        List<SurveyParticipationTimeSlot> slots =
                timeSlotRepository.findOverlappingWindowWithSurvey(rangeStart, rangeEndExclusive);
        List<Object[]> participationTuples =
                participationRepository.findRespondentSurveyDateTuplesInWindow(rangeStart, rangeEndExclusive);
        List<OffsetDateTime> locationDates =
                localizationDataRepository.findAllDateTimesInWindow(rangeStart, rangeEndExclusive);
        List<OffsetDateTime> sensorDates =
                sensorDataRepository.findAllDateTimesInWindow(rangeStart, rangeEndExclusive);
        List<OffsetDateTime> outsideAreaDates =
                participationRepository.findDatesOutsideResearchAreaInWindow(rangeStart, rangeEndExclusive);

        Map<LocalDate, List<Object[]>> participationsByDay = participationTuples.stream()
                .collect(Collectors.groupingBy(tuple -> toUtcDate((OffsetDateTime) tuple[2])));
        Map<LocalDate, Long> locationByDay = countByUtcDay(locationDates);
        Map<LocalDate, Long> sensorByDay = countByUtcDay(sensorDates);
        Map<LocalDate, Long> outsideByDay = countByUtcDay(outsideAreaDates);

        List<DailyStatsRowDto> rows = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            final LocalDate currentDay = day;
            OffsetDateTime dayStart = currentDay.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            OffsetDateTime dayEnd = dayStart.plusDays(1);
            long slotsThatDay = slots.stream()
                    .filter(slot -> !slot.getStart().isAfter(dayEnd) && !slot.getFinish().isBefore(dayStart))
                    .count();

            long availableRespondents = respondents.stream()
                    .filter(u -> !u.hasSurveyWindow() || u.isActiveOn(currentDay))
                    .count();
            List<UUID> setDatesIds = respondents.stream()
                    .filter(u -> u.isActiveOn(currentDay))
                    .map(IdentityUser::getId)
                    .toList();
            Set<UUID> setDatesSet = new HashSet<>(setDatesIds);

            List<Object[]> dayParticipations = participationsByDay.getOrDefault(currentDay, List.of());
            long surveysFilled = dayParticipations.size();
            Set<UUID> respondentsWithSubmission = dayParticipations.stream()
                    .map(tuple -> (UUID) tuple[0])
                    .collect(Collectors.toSet());
            long surveysFilledActive = dayParticipations.stream()
                    .filter(tuple -> setDatesSet.contains((UUID) tuple[0]))
                    .count();

            rows.add(new DailyStatsRowDto(
                    currentDay,
                    respondentsWithSubmission.size(),
                    surveysFilled,
                    availableRespondents * slotsThatDay,
                    surveysFilledActive,
                    setDatesIds.size() * slotsThatDay,
                    setDatesIds.size(),
                    locationByDay.getOrDefault(currentDay, 0L),
                    sensorByDay.getOrDefault(currentDay, 0L),
                    outsideByDay.getOrDefault(currentDay, 0L)));
        }
        return rows;
    }

    private static Map<LocalDate, Long> countByUtcDay(List<OffsetDateTime> timestamps) {
        return timestamps.stream()
                .collect(Collectors.groupingBy(
                        StatisticsServiceImpl::toUtcDate,
                        Collectors.counting()));
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

        List<DailyCompletionRespondentDto> respondentDtos = respondents.stream()
                .map(user -> toRespondentDto(
                        user,
                        completedByRespondent.getOrDefault(user.getId(), Set.of()),
                        slots,
                        locationDatesByRespondent.getOrDefault(user.getId(), List.of()),
                        sensorDatesByRespondent.getOrDefault(user.getId(), List.of())))
                .sorted(Comparator.comparing(
                        (DailyCompletionRespondentDto dto) -> dto.username() == null ? "" : dto.username(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new DailyCompletionOverviewDto(date, timeSlotDtos, respondentDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public IssuesOverviewDto getIssuesOverview(IssuesRangeMode rangeMode, LocalDate from, LocalDate to) {
        if (rangeMode == IssuesRangeMode.custom) {
            if (from == null || to == null) {
                throw new IllegalArgumentException("'from' and 'to' are required when rangeMode=custom.");
            }
            if (to.isBefore(from)) {
                throw new IllegalArgumentException("'to' must be on or after 'from'.");
            }
        }

        List<IdentityUser> respondents = identityUserRepository.findByRole(Role.RESPONDENT.getRoleName());
        List<Object[]> slotWindows = timeSlotRepository.findAllActiveSlotWindows();
        Set<UUID> gpsLinked = new HashSet<>(localizationDataRepository.findLinkedParticipationIds());
        Set<UUID> sensorLinked = new HashSet<>(sensorDataRepository.findLinkedParticipationIds());

        List<Object[]> participationTuples = rangeMode == IssuesRangeMode.custom
                ? participationRepository.findRespondentParticipationTuplesInWindow(
                        from.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(),
                        to.atTime(23, 59, 59).atOffset(ZoneOffset.UTC))
                : participationRepository.findAllRespondentParticipationTuples();

        Map<UUID, List<ParticipationRow>> byRespondent = new HashMap<>();
        for (Object[] row : participationTuples) {
            UUID respondentId = (UUID) row[0];
            UUID participationId = (UUID) row[1];
            OffsetDateTime date = (OffsetDateTime) row[2];
            byRespondent
                    .computeIfAbsent(respondentId, ignored -> new ArrayList<>())
                    .add(new ParticipationRow(participationId, date));
        }

        List<RespondentIssueDto> rows = new ArrayList<>();
        for (IdentityUser user : respondents) {
            if (!user.hasSurveyWindow()) {
                continue;
            }

            LocalDate windowStart;
            LocalDate windowEnd;
            if (rangeMode == IssuesRangeMode.survey_window) {
                windowStart = user.getSurveyStartDate();
                windowEnd = user.getSurveyEndDate();
            } else {
                LocalDate intersectionStart = maxDate(from, user.getSurveyStartDate());
                LocalDate intersectionEnd = minDate(to, user.getSurveyEndDate());
                if (intersectionEnd.isBefore(intersectionStart)) {
                    continue;
                }
                windowStart = intersectionStart;
                windowEnd = intersectionEnd;
            }

            OffsetDateTime windowStartOd = windowStart.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            OffsetDateTime windowEndOd = windowEnd.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

            long available = countSlotsOverlapping(slotWindows, windowStartOd, windowEndOd);
            if (rangeMode == IssuesRangeMode.custom && available == 0) {
                continue;
            }

            List<ParticipationRow> inWindow = byRespondent
                    .getOrDefault(user.getId(), List.of())
                    .stream()
                    .filter(p -> !p.date().isBefore(windowStartOd) && !p.date().isAfter(windowEndOd))
                    .toList();

            long filled = inWindow.size();
            long gpsFilled = inWindow.stream().filter(p -> gpsLinked.contains(p.id())).count();
            long sensorFilled = inWindow.stream().filter(p -> sensorLinked.contains(p.id())).count();
            long skipped = Math.max(0, available - filled);

            rows.add(new RespondentIssueDto(
                    user.getId(),
                    user.getUsername(),
                    windowStart,
                    windowEnd,
                    filled,
                    available,
                    gpsFilled,
                    sensorFilled,
                    skipped,
                    percentOrNull(filled, available),
                    percentOrNull(gpsFilled, available),
                    percentOrNull(sensorFilled, available)));
        }

        rows.sort(Comparator.comparing(
                dto -> dto.username() == null ? "" : dto.username(),
                String.CASE_INSENSITIVE_ORDER));

        return new IssuesOverviewDto(rows, rows.size());
    }

    private static LocalDate maxDate(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDate minDate(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    private static long countSlotsOverlapping(
            List<Object[]> slotWindows, OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        long count = 0;
        for (Object[] slot : slotWindows) {
            OffsetDateTime start = (OffsetDateTime) slot[0];
            OffsetDateTime finish = (OffsetDateTime) slot[1];
            if (!start.isAfter(windowEnd) && !finish.isBefore(windowStart)) {
                count++;
            }
        }
        return count;
    }

    private static Double percentOrNull(long filled, long available) {
        if (available <= 0) {
            return null;
        }
        return (filled * 100.0) / available;
    }

    private record ParticipationRow(UUID id, OffsetDateTime date) {}

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
            List<OffsetDateTime> sensorDates) {
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
                user.getSurveyStartDate(), user.getSurveyEndDate());
    }

    private ParticipantStatsDto toParticipantStats(Object[] row, IdentityUser user) {
        UUID respondentId = (UUID) row[0];
        String username = (String) row[1];
        OffsetDateTime firstParticipation = (OffsetDateTime) row[2];
        OffsetDateTime lastParticipation = (OffsetDateTime) row[3];
        long surveysFilled = ((Number) row[4]).longValue();

        OffsetDateTime windowStart = firstParticipation;
        OffsetDateTime windowEnd = lastParticipation;
        if (user != null && user.hasSurveyWindow()) {
            windowStart = user.getSurveyStartDate().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            windowEnd = user.getSurveyEndDate().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        }

        long surveysAvailable = timeSlotRepository.countOverlappingWindow(windowStart, windowEnd);
        long locationDataCount = localizationDataRepository.countByIdentityUserId(respondentId);
        long sensorDataCount = sensorDataRepository.countByRespondentId(respondentId);
        long outsideResearchAreaCount =
                participationRepository.countOutsideResearchAreaByRespondentId(respondentId);
        return new ParticipantStatsDto(
                respondentId, username,
                windowStart, windowEnd,
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
