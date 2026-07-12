package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
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
}
