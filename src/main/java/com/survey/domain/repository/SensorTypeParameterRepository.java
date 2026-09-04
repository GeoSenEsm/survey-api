package com.survey.domain.repository;

import com.survey.domain.models.SensorTypeParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorTypeParameterRepository extends JpaRepository<SensorTypeParameter, UUID> {
    List<SensorTypeParameter> findBySensorTypeIdOrderByCode(UUID sensorTypeId);

    Optional<SensorTypeParameter> findBySensorTypeIdAndCode(UUID sensorTypeId, String code);

    boolean existsBySensorTypeIdAndCode(UUID sensorTypeId, String code);

    long countByUsedParameterId(UUID usedParameterId);
}
