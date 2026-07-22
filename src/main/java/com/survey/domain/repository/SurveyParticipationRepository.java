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

    @Query("SELECT DISTINCT sp FROM SurveyParticipation sp " +
            "LEFT JOIN FETCH sp.survey " +
            "LEFT JOIN FETCH sp.questionAnswers " +
            "WHERE sp.identityUser.id IN :identityUserIds")
    List<SurveyParticipation> findAllByIdentityUserIdsWithFetch(List<UUID> identityUserIds);

    /**
     * One row per respondent that has at least one participation:
     * {@code [respondentId, username, minDate, maxDate, count]}. Used by
     * the statistics service.
     */
    @Query("SELECT sp.identityUser.id, sp.identityUser.username, " +
            "MIN(sp.date), MAX(sp.date), COUNT(sp) " +
            "FROM SurveyParticipation sp " +
            "GROUP BY sp.identityUser.id, sp.identityUser.username")
    List<Object[]> aggregateParticipationsPerRespondent();

    @Query("SELECT sp.date FROM SurveyParticipation sp " +
            "WHERE sp.identityUser.id = :respondentId " +
            "ORDER BY sp.date")
    List<OffsetDateTime> findDatesByRespondentId(UUID respondentId);

    @Query("SELECT sp.date FROM SurveyParticipation sp ORDER BY sp.date")
    List<OffsetDateTime> findAllDatesOrdered();

    /**
     * One row per participation whose {@code date} falls in {@code [from, to]}:
     * {@code [respondentId, surveyId, date]}. Used by the daily-completion
     * overview to map filled slots to respondents without hydrating full
     * entity graphs.
     */
    @Query("SELECT sp.identityUser.id, sp.survey.id, sp.date FROM SurveyParticipation sp " +
            "WHERE sp.date BETWEEN :from AND :to")
    List<Object[]> findRespondentSurveyDateTuplesInWindow(OffsetDateTime from, OffsetDateTime to);

    /**
     * Submission timestamps of participations whose linked localization
     * data was captured outside the configured research area polygon.
     * The join relies on {@code LocalizationData.surveyParticipation}
     * (unique per participation since V14) and its precomputed
     * {@code outsideResearchArea} bit. Participations without any linked
     * localization row are naturally excluded.
     */
    @Query("SELECT sp.date FROM SurveyParticipation sp " +
            "JOIN LocalizationData ld ON ld.surveyParticipation = sp " +
            "WHERE ld.outsideResearchArea = true " +
            "AND sp.date BETWEEN :from AND :to " +
            "ORDER BY sp.date")
    List<OffsetDateTime> findDatesOutsideResearchAreaInWindow(
            OffsetDateTime from, OffsetDateTime to);

    /**
     * Per-respondent version of {@link #findDatesOutsideResearchAreaInWindow}
     * used for the per-participant time series in the statistics view.
     */
    @Query("SELECT sp.date FROM SurveyParticipation sp " +
            "JOIN LocalizationData ld ON ld.surveyParticipation = sp " +
            "WHERE ld.outsideResearchArea = true " +
            "AND sp.identityUser.id = :respondentId " +
            "AND sp.date BETWEEN :from AND :to " +
            "ORDER BY sp.date")
    List<OffsetDateTime> findDatesOutsideResearchAreaForRespondentInWindow(
            UUID respondentId, OffsetDateTime from, OffsetDateTime to);

    /**
     * Total number of participations by {@code respondentId} whose linked
     * localization was captured outside the research area polygon.
     * Feeds the per-participant KPI card.
     */
    @Query("SELECT COUNT(sp) FROM SurveyParticipation sp " +
            "JOIN LocalizationData ld ON ld.surveyParticipation = sp " +
            "WHERE ld.outsideResearchArea = true " +
            "AND sp.identityUser.id = :respondentId")
    long countOutsideResearchAreaByRespondentId(UUID respondentId);
}
