package com.survey.domain.repository;

import com.survey.domain.models.InitialSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface InitialSurveyRepository extends JpaRepository<InitialSurvey, UUID> {
    @Query(value = "SELECT TOP 1 * FROM initial_survey ORDER BY row_version DESC", nativeQuery = true)
    Optional<InitialSurvey> findTopByRowVersionDesc();
}