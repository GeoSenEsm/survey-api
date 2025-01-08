package com.survey.application.dtos.surveyDtos;

import com.survey.application.dtos.SensorDataDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SendSurveyResponseDto {
    UUID getSurveyId();
    OffsetDateTime getStartDate();
    OffsetDateTime getFinishDate();
    SensorDataDto getSensorData();
    List<AnswerDto> getAnswers();
    SendSurveyResponseDto setSurveyId(UUID surveyId);
    SendSurveyResponseDto setStartDate(OffsetDateTime startDate);
    SendSurveyResponseDto setFinishDate(OffsetDateTime finishDate);
    SendSurveyResponseDto setSensorData(SensorDataDto sensorData);
    SendSurveyResponseDto setAnswers(List<AnswerDto> answers);
}
