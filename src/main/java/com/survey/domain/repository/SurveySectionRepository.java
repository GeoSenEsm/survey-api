package com.survey.domain.repository;

import com.survey.domain.models.SurveySection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurveySectionRepository extends JpaRepository<SurveySection, UUID> {
}
