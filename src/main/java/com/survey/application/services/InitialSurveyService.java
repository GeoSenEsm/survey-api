package com.survey.application.services;

import com.survey.application.dtos.initialSurvey.CreateInitialSurveyDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyResponseDto;

import java.util.List;
import java.util.UUID;

public interface InitialSurveyService {
    InitialSurveyResponseDto createInitialSurvey(CreateInitialSurveyDto createInitialSurveyDto);

    List<InitialSurveyResponseDto> getInitialSurveys();

    InitialSurveyResponseDto getInitialSurveyById(UUID surveyId);
}
