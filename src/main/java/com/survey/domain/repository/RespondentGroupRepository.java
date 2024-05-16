package com.survey.domain.repository;

import com.survey.domain.models.RespondentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RespondentGroupRepository extends JpaRepository<RespondentGroup, UUID> {
}
