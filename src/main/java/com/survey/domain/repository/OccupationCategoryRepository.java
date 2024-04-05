package com.survey.domain.repository;

import com.survey.domain.models.OccupationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccupationCategoryRepository extends JpaRepository<OccupationCategory, Integer> {
}
