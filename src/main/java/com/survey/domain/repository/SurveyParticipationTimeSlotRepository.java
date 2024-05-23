package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurveyParticipationTimeSlotRepository extends JpaRepository<SurveyParticipationTimeSlot, UUID> {
}
