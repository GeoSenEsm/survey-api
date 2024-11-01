package com.survey.domain.repository;

import com.survey.domain.models.LocalizationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocalizationDataRepository extends JpaRepository<LocalizationData, UUID> {
    @Query(value = "SELECT id, respondent_id, participation_id, date_time, " +
            "localization.STAsText() AS localization, row_version " +
            "FROM localization_data " +
            "WHERE date_time BETWEEN :from AND :to", nativeQuery = true)
    List<LocalizationData> findAllBetween(OffsetDateTime from, OffsetDateTime to);


    @Query(value = "SELECT id, respondent_id, participation_id, date_time, " +
            "localization.STAsText() AS localization, row_version " +
            "FROM localization_data " +
            "WHERE respondent_id = :respondentId", nativeQuery = true)
    List<LocalizationData> findByRespondentId(UUID respondentId);

}
