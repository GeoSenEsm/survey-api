package com.survey.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.domain.models.QualityOfSleep;

public interface QualityOfSleepRepository extends JpaRepository<QualityOfSleep, Integer> {

}
