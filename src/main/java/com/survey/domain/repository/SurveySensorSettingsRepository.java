package com.survey.domain.repository;

import com.survey.domain.models.SurveySensorSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveySensorSettingsRepository extends JpaRepository<SurveySensorSettings, Integer> {
}
