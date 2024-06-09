package com.survey.domain.repository;

import com.survey.domain.models.SurveySendingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SurveySendingPolicyRepository extends JpaRepository<SurveySendingPolicy, UUID> {

    List<SurveySendingPolicy> findAllBySurveyId(UUID surveyId);
}
