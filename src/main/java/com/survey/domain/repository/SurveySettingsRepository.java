package com.survey.domain.repository;

import com.survey.domain.models.SurveySettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveySettingsRepository extends JpaRepository<SurveySettings, Integer> {
}
