package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveyDto;
import com.survey.application.dtos.surveyDtos.ResponseSurveyDto;
import com.survey.application.dtos.surveyDtos.ResponseSurveyShortDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SurveyService {

    ResponseSurveyDto createSurvey(CreateSurveyDto createSurveyDto);
    List<ResponseSurveyDto> getSurveysByCompletionDate(LocalDate completionDate);
    List<ResponseSurveyShortDto> getSurveysShort();
    ResponseSurveyDto getSurveyById(UUID surveyId);
}
