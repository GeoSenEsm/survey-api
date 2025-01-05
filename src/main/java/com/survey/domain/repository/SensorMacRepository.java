package com.survey.domain.repository;

import com.survey.domain.models.SensorMac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorMacRepository extends JpaRepository<SensorMac, UUID> {
    @Query("SELECT sm FROM SensorMac sm WHERE sm.sensorId = :sensorId")
    Optional<SensorMac> findBySensorId(String sensorId);

    @Query("SELECT sm FROM SensorMac sm ORDER BY sm.sensorId")
    List<SensorMac> findAllOrderBySensorId();
}

