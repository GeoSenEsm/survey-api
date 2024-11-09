package com.survey.domain.repository;

import com.survey.domain.models.ResearchArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResearchAreaRepository extends JpaRepository<ResearchArea, UUID> {
    Optional<ResearchArea> findFirstByOrderByIdAsc();
}
