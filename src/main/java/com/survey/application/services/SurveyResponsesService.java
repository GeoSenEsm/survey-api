package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;

import javax.management.InvalidAttributeValueException;

public interface SurveyResponsesService {
    SurveyParticipationDto saveSurveyResponse(SendSurveyResponseDto sendSurveyResponseDto, String token) throws InvalidAttributeValueException;
}
