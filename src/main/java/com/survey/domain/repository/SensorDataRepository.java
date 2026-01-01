package com.survey.domain.repository;

import com.survey.domain.models.SensorData;
import com.survey.domain.models.IdentityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorDataRepository extends JpaRepository<SensorData, UUID> {
    @Query("SELECT sd " +
            "FROM SensorData sd " +
            "WHERE sd.dateTime BETWEEN :fromDate AND :toDate " +
            "ORDER BY sd.respondent.id, sd.dateTime")
    List<SensorData> findAllBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

    @Query(value = "SELECT MAX(sd.dateTime) FROM SensorData sd WHERE sd.respondent.id = :respondentId")
    Optional<OffsetDateTime> findDateOfLastEntryForRespondent(UUID respondentId);

    List<SensorData> findAllByRespondent(IdentityUser respondent);
}
