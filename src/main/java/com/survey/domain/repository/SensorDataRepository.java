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

    @Query("SELECT DISTINCT sd FROM SensorData sd " +
            "LEFT JOIN FETCH sd.surveyParticipation " +
            "WHERE sd.respondent.id IN :respondentIds")
    List<SensorData> findAllByRespondentIdsWithFetch(List<UUID> respondentIds);

    long countByRespondentId(UUID respondentId);

    @Query("SELECT sd.dateTime FROM SensorData sd " +
            "WHERE sd.respondent.id = :respondentId " +
            "AND sd.dateTime BETWEEN :from AND :to")
    List<OffsetDateTime> findDateTimesForRespondentInWindow(
            UUID respondentId, OffsetDateTime from, OffsetDateTime to);

    @Query("SELECT sd.dateTime FROM SensorData sd " +
            "WHERE sd.dateTime BETWEEN :from AND :to")
    List<OffsetDateTime> findAllDateTimesInWindow(OffsetDateTime from, OffsetDateTime to);

    @Query("SELECT sd.respondent.id, sd.dateTime FROM SensorData sd " +
            "WHERE sd.dateTime BETWEEN :from AND :to")
    List<Object[]> findRespondentDateTimesInWindow(OffsetDateTime from, OffsetDateTime to);
}
