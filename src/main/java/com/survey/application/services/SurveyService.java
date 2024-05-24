package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveyDto;
import com.survey.application.dtos.surveyDtos.ResponseSurveyDto;

public interface SurveyService {

    ResponseSurveyDto createSurvey(CreateSurveyDto createSurveyDto);
}
