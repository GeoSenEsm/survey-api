package com.survey.domain.repository;

import com.survey.domain.models.SensorParameterSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SensorParameterSourceRepository extends JpaRepository<SensorParameterSource, UUID> {
}
