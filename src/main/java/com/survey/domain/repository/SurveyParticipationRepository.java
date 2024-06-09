package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.UUID;
import java.util.List;

public interface SurveyParticipationRepository extends JpaRepository<SurveyParticipation, UUID> {

    List<SurveyParticipation> findAllBySurveyIdAndDate(UUID surveyId, Date date);
}
