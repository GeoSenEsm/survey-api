package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SendSurveyResponseDto {
    private UUID surveyId;
    private List<AnswerDto> answers;
}
