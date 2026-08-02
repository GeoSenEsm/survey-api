package com.survey.domain.repository;

import com.survey.domain.models.SensorDeviceSecret;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SensorDeviceSecretRepository extends JpaRepository<SensorDeviceSecret, UUID> {
    Optional<SensorDeviceSecret> findBySensorMacIdAndSecretName(UUID sensorMacId, String secretName);

    @EntityGraph(attributePaths = "sensorMac")
    List<SensorDeviceSecret> findBySensorMacIdIn(Set<UUID> sensorMacIds);
}
