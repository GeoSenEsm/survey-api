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

    /**
     * Same lookup as {@link #findTopByRowVersionDesc()}, but takes an exclusive, held-until-commit
     * lock on the row (SQL Server UPDLOCK + HOLDLOCK hints). Used to make the "is the study
     * published yet" check-then-act sequence atomic across concurrent requests: whichever caller
     * (a sensor-setup mutation or the publish action itself) reads the row first blocks the other
     * until its own transaction commits.
     */
    @Query(value = "SELECT TOP 1 * FROM initial_survey WITH (UPDLOCK, HOLDLOCK) ORDER BY row_version DESC", nativeQuery = true)
    Optional<InitialSurvey> findTopByRowVersionDescForUpdate();
}