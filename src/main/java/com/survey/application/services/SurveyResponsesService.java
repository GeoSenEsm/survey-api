package com.survey.application.services;

import com.survey.application.dtos.SurveyResultDto;
import com.survey.application.dtos.surveyDtos.SendOfflineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SendOnlineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;

import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SurveyResponsesService {
    SurveyParticipationDto saveSurveyResponseOnline(SendOnlineSurveyResponseDto sendOnlineSurveyResponseDto);
    List<SurveyParticipationDto> saveSurveyResponsesOffline(List<SendOfflineSurveyResponseDto> sendOfflineSurveyResponseDtoList);
    List<SurveyResultDto> getSurveyResults(UUID surveyId, UUID identityUserId, OffsetDateTime dateFrom, OffsetDateTime dateTo, Boolean outsideResearchArea);
    List<SurveyResultDto> getSurveyResultsBatch(UUID surveyId, UUID identityUserId, OffsetDateTime dateFrom, OffsetDateTime dateTo, Boolean outsideResearchArea, int offset, int limit);
    void streamSurveyResults(OutputStream outputStream, UUID surveyId, UUID identityUserId, OffsetDateTime dateFrom, OffsetDateTime dateTo, Boolean outsideResearchArea) throws Exception;
    void streamAllSurveyResults(OutputStream outputStream) throws Exception;
}
