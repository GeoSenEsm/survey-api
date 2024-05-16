package com.survey.domain.repository;

import com.survey.domain.models.RespondentToGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RespondentToGroupRepository extends JpaRepository<RespondentToGroup, UUID> {
}
