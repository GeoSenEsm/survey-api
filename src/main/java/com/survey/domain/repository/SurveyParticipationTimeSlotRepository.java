package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SurveyParticipationTimeSlotRepository extends JpaRepository<SurveyParticipationTimeSlot, UUID> {
    List<SurveyParticipationTimeSlot> findByFinishBetween(OffsetDateTime startOfDay, OffsetDateTime endOfDay);
    long countByIdIn(List<UUID> ids);

    /**
     * Count of non-deleted time slots whose {@code [start, finish]} range
     * overlaps the given window. Overlap = {@code start <= windowEnd AND
     * finish >= windowStart}. Represents "how many survey response
     * opportunities the participant had during their study window".
     */
    @Query("SELECT COUNT(ts) FROM SurveyParticipationTimeSlot ts " +
            "WHERE ts.isDeleted = false " +
            "AND ts.start <= :windowEnd " +
            "AND ts.finish >= :windowStart")
    long countOverlappingWindow(OffsetDateTime windowStart, OffsetDateTime windowEnd);

    /**
     * Non-deleted time slots whose {@code [start, finish]} range overlaps
     * the given window, together with the parent survey eagerly loaded so
     * callers can read {@code timeSlot.getSurveySendingPolicy().getSurvey()}
     * without triggering N+1 lazy loads. Sorted by {@code start} to keep
     * the overview grid deterministic.
     */
    @Query("SELECT ts FROM SurveyParticipationTimeSlot ts " +
            "JOIN FETCH ts.surveySendingPolicy p " +
            "JOIN FETCH p.survey " +
            "WHERE ts.isDeleted = false " +
            "AND ts.start <= :windowEnd " +
            "AND ts.finish >= :windowStart " +
            "ORDER BY ts.start")
    List<SurveyParticipationTimeSlot> findOverlappingWindowWithSurvey(
            OffsetDateTime windowStart, OffsetDateTime windowEnd);

    @Query("SELECT ts.start, ts.finish FROM SurveyParticipationTimeSlot ts " +
            "WHERE ts.isDeleted = false")
    List<Object[]> findAllActiveSlotWindows();
}
