package com.survey.domain.repository;

import com.survey.domain.models.LifeSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LifeSatisfactionRepository extends JpaRepository<LifeSatisfaction, Integer> {
}
