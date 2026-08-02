package com.survey.domain.repository;

import com.survey.domain.models.SensorTypeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SensorTypeSettingRepository extends JpaRepository<SensorTypeSetting, UUID> {
    @Query("SELECT sts FROM SensorTypeSetting sts JOIN FETCH sts.sensorType ORDER BY sts.displayOrder")
    List<SensorTypeSetting> findAllOrdered();

    void deleteAllBySensorTypeIdIn(List<UUID> sensorTypeIds);
}
