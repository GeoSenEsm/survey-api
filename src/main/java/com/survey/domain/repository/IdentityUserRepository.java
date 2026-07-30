package com.survey.domain.repository;

import com.survey.domain.models.IdentityUser;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityUserRepository extends JpaRepository<IdentityUser, UUID> {
    Optional<IdentityUser> findByUsername(String username);
    @Query("select count(u) from IdentityUser u where u.role = 'Respondent'")
    int countRespondents();

    @Query("SELECT count(u) > 0 FROM IdentityUser u where u.id = :id and u.role = 'Respondent'")
    boolean existsById(@NonNull UUID id);

    List<IdentityUser> findByRole(String roleName);

    @Query("SELECT u FROM IdentityUser u WHERE u.role = 'Respondent' "
            + "AND u.surveyStartDate IS NOT NULL AND u.surveyEndDate IS NOT NULL")
    List<IdentityUser> findRespondentsWithSurveyWindow();

    @Query("SELECT MIN(u.surveyStartDate) FROM IdentityUser u WHERE u.role = 'Respondent' "
            + "AND u.surveyStartDate IS NOT NULL")
    LocalDate findEarliestSurveyStartDate();

    @Query("SELECT MAX(u.surveyEndDate) FROM IdentityUser u WHERE u.role = 'Respondent' "
            + "AND u.surveyEndDate IS NOT NULL")
    LocalDate findLatestSurveyEndDate();

    @Query("SELECT COUNT(u) FROM IdentityUser u WHERE u.role = 'Respondent' AND ("
            + "u.surveyStartDate IS NULL OR u.surveyEndDate IS NULL "
            + "OR (u.surveyStartDate <= :day AND u.surveyEndDate >= :day))")
    long countRespondentsAvailableOn(LocalDate day);

    @Query("SELECT u.id FROM IdentityUser u WHERE u.role = 'Respondent' "
            + "AND u.surveyStartDate IS NOT NULL AND u.surveyEndDate IS NOT NULL "
            + "AND u.surveyStartDate <= :day AND u.surveyEndDate >= :day")
    List<UUID> findRespondentIdsWithWindowCovering(LocalDate day);
}
