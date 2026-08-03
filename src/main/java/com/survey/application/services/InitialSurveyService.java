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

    /**
     * Once the initial survey is published, respondent groups have already been created from it
     * and the study is considered live. Other services use this as the signal to stop accepting
     * changes that would otherwise apply retroactively to a running study (e.g. sensor data setup).
     */
    boolean isPublished();
}