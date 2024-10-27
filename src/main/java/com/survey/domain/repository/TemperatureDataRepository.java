package com.survey.domain.repository;

import com.survey.domain.models.TemperatureData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TemperatureDataRepository extends JpaRepository<TemperatureData, UUID> {
    @Query("SELECT td " +
            "FROM TemperatureData td " +
            "WHERE td.dateTime BETWEEN :fromDate AND :toDate " +
            "ORDER BY td.respondent.id, td.dateTime")
    List<TemperatureData> findAllBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
}
