package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurveyParticipationRepository extends JpaRepository<SurveyParticipation, UUID> {

}
