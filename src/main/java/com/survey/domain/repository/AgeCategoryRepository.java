package com.survey.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.survey.domain.models.AgeCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface AgeCategoryRepository extends JpaRepository<AgeCategory, Integer> {
    boolean existsByDisplay(String display);
}
