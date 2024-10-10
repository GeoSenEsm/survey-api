package com.survey.application.services;

import com.survey.application.dtos.SurveyResultDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;

import javax.management.InvalidAttributeValueException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SurveyResponsesService {
    SurveyParticipationDto saveSurveyResponse(SendSurveyResponseDto sendSurveyResponseDto, String token) throws InvalidAttributeValueException;
    List<SurveyResultDto> getSurveyResults(UUID surveyId, LocalDateTime dateFrom, LocalDateTime dateTo);
}
