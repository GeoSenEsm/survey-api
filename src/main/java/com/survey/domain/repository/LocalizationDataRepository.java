package com.survey.domain.repository;

import com.survey.domain.models.LocalizationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LocalizationDataRepository extends JpaRepository<LocalizationData, UUID> {
    @Query("SELECT COUNT(ld) > 0 FROM LocalizationData ld " +
            "WHERE ld.identityUser.id = :respondentId " +
            "AND ((:participationId IS NOT NULL AND ld.surveyParticipation.id = :participationId) " +
            "OR (:participationId IS NULL AND ld.surveyParticipation.id IS NULL))")
    boolean existsByRespondentIdAndParticipationId(
            @Param("respondentId") UUID respondentId,
            @Param("participationId") UUID participationId);

}
