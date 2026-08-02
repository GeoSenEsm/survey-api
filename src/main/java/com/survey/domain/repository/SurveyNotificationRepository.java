package com.survey.domain.repository;

import com.survey.domain.models.SurveyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SurveyNotificationRepository extends JpaRepository<SurveyNotification, UUID> {
    List<SurveyNotification> findAllBySurveyIdOrderByOrderAsc(UUID surveyId);

    void deleteAllBySurveyId(UUID surveyId);
}
