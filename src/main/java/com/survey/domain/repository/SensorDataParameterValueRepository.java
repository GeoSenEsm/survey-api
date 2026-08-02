package com.survey.domain.repository;

import com.survey.domain.models.SensorDataParameterValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SensorDataParameterValueRepository extends JpaRepository<SensorDataParameterValue, UUID> {
}
