package com.survey.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.domain.models.HealthCondition;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthConditionRepository extends JpaRepository<HealthCondition, Integer> {

}
