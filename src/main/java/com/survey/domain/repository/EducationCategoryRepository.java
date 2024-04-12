package com.survey.domain.repository;

import com.survey.domain.models.EducationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationCategoryRepository extends JpaRepository<EducationCategory, Integer> {
}
