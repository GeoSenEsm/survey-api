package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SurveyParticipationRepositoryCustom {
    List<SurveyParticipation> findByFiltersWithFetch(
            UUID surveyId,
            UUID identityUserId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Boolean outsideResearchArea
    );
}

