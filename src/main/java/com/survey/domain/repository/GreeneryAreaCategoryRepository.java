package com.survey.domain.repository;

import com.survey.domain.models.GreeneryAreaCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GreeneryAreaCategoryRepository extends JpaRepository<GreeneryAreaCategory, Integer> {
    boolean existsByDisplay(String display);

}
