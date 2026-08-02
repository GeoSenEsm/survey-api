package com.survey.domain.repository;

import com.survey.domain.models.SensorParameterDefinition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorParameterDefinitionRepository extends JpaRepository<SensorParameterDefinition, UUID> {
    Optional<SensorParameterDefinition> findByCode(String code);

    @EntityGraph(attributePaths = {"sources", "sources.sensorType"})
    @Query("SELECT spd FROM SensorParameterDefinition spd ORDER BY spd.displayOrder")
    List<SensorParameterDefinition> findAllOrderedWithSources();
}
