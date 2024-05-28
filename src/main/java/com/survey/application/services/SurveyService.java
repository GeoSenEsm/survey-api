package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveyDto;
import com.survey.application.dtos.surveyDtos.ResponseSurveyDto;

import java.time.LocalDate;
import java.util.List;

public interface SurveyService {

    ResponseSurveyDto createSurvey(CreateSurveyDto createSurveyDto);
    List<ResponseSurveyDto> getSurveysByCompletionDate(LocalDate completionDate);
}
