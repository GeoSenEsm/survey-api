package com.survey.domain.repository;

import com.survey.domain.models.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    boolean existsInSurveyById(UUID Id);

    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.surveySections WHERE s.id = :surveyId")
    Survey getSurveyById(@Param("surveyId") UUID surveyId);
}
