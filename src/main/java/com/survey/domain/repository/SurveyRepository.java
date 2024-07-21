package com.survey.domain.repository;

import com.survey.domain.models.Survey;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    @EntityGraph(attributePaths = {"sections.questions"})
    Optional<Survey> findById(UUID id);
}
