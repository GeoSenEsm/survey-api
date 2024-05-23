package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveyRequestDto;
import com.survey.application.dtos.surveyDtos.ResponseSurveyRequestDto;

public interface SurveyService {

    ResponseSurveyRequestDto createSurvey(CreateSurveyRequestDto createSurveyRequestDto);
}
