package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveyRequestDto;

public interface SurveyService {

    void createSurvey(CreateSurveyRequestDto createSurveyRequestDto);
}
