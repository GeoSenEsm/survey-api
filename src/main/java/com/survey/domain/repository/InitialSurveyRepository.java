package com.survey.domain.repository;

import com.survey.domain.models.InitialSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface InitialSurveyRepository extends JpaRepository<InitialSurvey, UUID> {
}
