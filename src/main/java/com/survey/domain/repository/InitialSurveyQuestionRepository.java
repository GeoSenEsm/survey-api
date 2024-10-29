package com.survey.domain.repository;

import com.survey.domain.models.InitialSurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InitialSurveyQuestionRepository extends JpaRepository<InitialSurveyQuestion, UUID> {
    @Query("SELECT q FROM InitialSurveyQuestion q WHERE q.initialSurvey.id = :surveyId AND q.id IN :questionIds")
    List<InitialSurveyQuestion> findAllByIds(UUID surveyId, List<UUID> questionIds);
}
