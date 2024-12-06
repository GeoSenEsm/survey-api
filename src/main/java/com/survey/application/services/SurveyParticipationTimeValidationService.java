package com.survey.application.services;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface SurveyParticipationTimeValidationService {
    OffsetDateTime getCorrectSurveyParticipationDateTimeOnline(UUID identityUserId, UUID surveyId, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate);
    OffsetDateTime getCorrectSurveyParticipationDateTimeOffline(UUID identityUserId, UUID surveyId, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate);
}
