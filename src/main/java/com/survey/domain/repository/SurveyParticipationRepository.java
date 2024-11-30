package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface SurveyParticipationRepository extends JpaRepository<SurveyParticipation, UUID> {

    List<SurveyParticipation> findAllBySurveyIdAndDate(UUID surveyId, Date date);

    @Query("SELECT sp FROM SurveyParticipation sp WHERE sp.id = :surveyParticipationId AND sp.identityUser.id = :respondentId")
    Optional<SurveyParticipation> findByIdAndRespondentId(UUID surveyParticipationId, UUID respondentId);

    @Query("SELECT COUNT(sp) > 0 FROM SurveyParticipation sp " +
            "WHERE sp.survey.id = :surveyId " +
            "AND sp.identityUser.id = :respondentId " +
            "AND sp.date BETWEEN :startDate AND :endDate")
    boolean existsBySurveyIdAndRespondentIdAndDateBetween(UUID surveyId, UUID respondentId, OffsetDateTime startDate, OffsetDateTime endDate);

}
