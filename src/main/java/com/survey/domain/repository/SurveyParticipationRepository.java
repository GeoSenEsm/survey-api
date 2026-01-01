package com.survey.domain.repository;

import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SurveyParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyParticipationRepository extends JpaRepository<SurveyParticipation, UUID>, SurveyParticipationRepositoryCustom {

    List<SurveyParticipation> findAllBySurveyIdAndDate(UUID surveyId, Date date);

    @Query("SELECT sp FROM SurveyParticipation sp WHERE sp.id = :surveyParticipationId AND sp.identityUser.id = :identityUserId")
    Optional<SurveyParticipation> findByIdAndIdentityUserId(UUID surveyParticipationId, UUID identityUserId);

    @Query("SELECT COUNT(sp) > 0 FROM SurveyParticipation sp " +
            "WHERE sp.survey.id = :surveyId " +
            "AND sp.identityUser.id = :identityUserId " +
            "AND sp.date BETWEEN :startDate AND :endDate")
    boolean existsBySurveyIdAndIdentityUserIdAndDateBetween(UUID surveyId, UUID identityUserId, OffsetDateTime startDate, OffsetDateTime endDate);

    List<SurveyParticipation> findAllByIdentityUser(IdentityUser identityUser);
}
