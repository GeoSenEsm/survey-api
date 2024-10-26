package com.survey.application.services;

import com.survey.application.dtos.initialSurvey.CreateInitialSurveyDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyResponseDto;

public interface InitialSurveyService {
    InitialSurveyResponseDto createInitialSurvey(CreateInitialSurveyDto createInitialSurveyDto);

    InitialSurveyResponseDto getInitialSurvey();
}
