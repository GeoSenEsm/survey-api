package com.survey.domain.repository;

import com.survey.domain.models.Survey;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    @EntityGraph(attributePaths = {"sections.questions", "sections.sectionToUserGroups"})
    Optional<Survey> findById(UUID id);

    @Query("SELECT s.name FROM Survey s WHERE s.id = :surveyId")
    String findSurveyNameBySurveyId(UUID surveyId);
}
