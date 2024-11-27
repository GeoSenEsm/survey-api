package com.survey.application.dtos.surveyDtos;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SendSurveyResponseDto {
    UUID getSurveyId();
    OffsetDateTime getStartDate();
    OffsetDateTime getFinishDate();
    List<AnswerDto> getAnswers();
}
