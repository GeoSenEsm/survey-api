package com.survey.domain.repository;

import com.survey.domain.models.LocalizationData;
import com.survey.domain.models.IdentityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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

    List<LocalizationData> findAllByIdentityUser(IdentityUser identityUser);

    @Query("SELECT DISTINCT ld FROM LocalizationData ld " +
            "LEFT JOIN FETCH ld.surveyParticipation " +
            "WHERE ld.identityUser.id IN :identityUserIds")
    List<LocalizationData> findAllByIdentityUserIdsWithFetch(List<UUID> identityUserIds);

    long countByIdentityUserId(UUID identityUserId);

    @Query("SELECT ld.dateTime FROM LocalizationData ld " +
            "WHERE ld.identityUser.id = :respondentId " +
            "AND ld.dateTime BETWEEN :from AND :to")
    List<OffsetDateTime> findDateTimesForRespondentInWindow(
            UUID respondentId, OffsetDateTime from, OffsetDateTime to);

    @Query("SELECT ld.dateTime FROM LocalizationData ld " +
            "WHERE ld.dateTime BETWEEN :from AND :to")
    List<OffsetDateTime> findAllDateTimesInWindow(OffsetDateTime from, OffsetDateTime to);

    @Query("SELECT ld.identityUser.id, ld.dateTime FROM LocalizationData ld " +
            "WHERE ld.dateTime BETWEEN :from AND :to")
    List<Object[]> findRespondentDateTimesInWindow(OffsetDateTime from, OffsetDateTime to);
}
