package com.survey.domain.repository;

import com.survey.domain.models.SectionToUserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SectionToUserGroupRepository extends JpaRepository<SectionToUserGroup, UUID> {
}
