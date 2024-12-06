package com.survey.application.services;

import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyStateDto;

import java.util.List;

public interface InitialSurveyService {
    List<InitialSurveyQuestionResponseDto> createInitialSurvey(List<CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList);
    List<InitialSurveyQuestionResponseDto> getInitialSurvey();
    InitialSurveyStateDto checkInitialSurveyState();
    void publishInitialSurveyAndCreateRespondentGroups();
}