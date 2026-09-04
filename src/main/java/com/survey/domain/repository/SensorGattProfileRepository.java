package com.survey.domain.repository;

import com.survey.domain.models.SensorGattProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;

public interface SensorGattProfileRepository extends JpaRepository<SensorGattProfile, UUID> {
    @EntityGraph(attributePaths = "sensorType")
    List<SensorGattProfile> findBySensorTypeIdOrderByRevisionDesc(UUID sensorTypeId);

    @EntityGraph(attributePaths = "sensorType")
    Optional<SensorGattProfile> findBySensorTypeIdAndRevision(UUID sensorTypeId, int revision);

    @EntityGraph(attributePaths = "sensorType")
    Optional<SensorGattProfile> findBySensorTypeIdAndStatus(UUID sensorTypeId, String status);

    @EntityGraph(attributePaths = "sensorType")
    List<SensorGattProfile> findBySensorTypeIdInAndStatus(Set<UUID> sensorTypeIds, String status);

}
