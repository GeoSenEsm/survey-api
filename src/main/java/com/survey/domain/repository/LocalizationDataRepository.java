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

    @Query("SELECT ld " +
            "FROM LocalizationData ld " +
            "WHERE ld.dateTime BETWEEN :fromDate AND :toDate " +
            "ORDER BY ld.dateTime")
    List<LocalizationData> findAllBetween(OffsetDateTime fromDate, OffsetDateTime toDate);


    @Query("SELECT ld " +
            "FROM LocalizationData ld " +
            "WHERE ld.identityUser.id = :respondentId")
    List<LocalizationData> findByRespondentId(UUID respondentId);

}
