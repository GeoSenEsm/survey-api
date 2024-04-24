package com.survey.domain.repository;

import com.survey.domain.models.RespondentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RespondentDataRepository extends JpaRepository<RespondentData, UUID> {
    boolean existsByIdentityUserId(UUID userId);
}
