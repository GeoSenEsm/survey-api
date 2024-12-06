package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SurveyParticipationTimeSlotRepository extends JpaRepository<SurveyParticipationTimeSlot, UUID> {
    List<SurveyParticipationTimeSlot> findByFinishBetween(OffsetDateTime startOfDay, OffsetDateTime endOfDay);
    long countByIdIn(List<UUID> ids);
}
