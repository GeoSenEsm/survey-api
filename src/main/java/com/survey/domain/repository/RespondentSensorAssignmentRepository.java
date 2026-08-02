package com.survey.domain.repository;

import com.survey.domain.models.RespondentSensorAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RespondentSensorAssignmentRepository extends JpaRepository<RespondentSensorAssignment, UUID> {
    @EntityGraph(attributePaths = {"respondent", "sensorType", "sensorMac"})
    @Query("SELECT rsa FROM RespondentSensorAssignment rsa ORDER BY rsa.respondent.username, rsa.priorityOrder")
    List<RespondentSensorAssignment> findAllOrdered();

    @EntityGraph(attributePaths = {"sensorType", "sensorMac"})
    List<RespondentSensorAssignment> findByRespondentIdAndEnabledTrueOrderByPriorityOrder(UUID respondentId);

    Optional<RespondentSensorAssignment> findBySensorMacId(UUID sensorMacId);

    Optional<RespondentSensorAssignment> findByRespondentIdAndSensorTypeId(UUID respondentId, UUID sensorTypeId);

    void deleteBySensorMacId(UUID sensorMacId);

    @Modifying
    @Query("DELETE FROM RespondentSensorAssignment rsa WHERE rsa.sensorMac IS NOT NULL")
    void deleteAllDeviceAssignments();
}
