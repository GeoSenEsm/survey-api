package com.survey.domain.repository;

import com.survey.domain.models.StressLevel;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StressLevelRepository extends JpaRepository<StressLevel, Integer> {
    boolean existsByDisplay(String display);
}
