package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveySectionDto;
import com.survey.domain.models.RespondentGroup;
import com.survey.domain.models.Survey;

import java.util.Dictionary;

public interface SurveyValidationService {
    void validateShowSections(Survey survey);
}
