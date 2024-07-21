package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.SendSurveyResponseDtoValidation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SendSurveyResponseDtoValidation
public class SendSurveyResponseDto {
    private UUID surveyId;
    private List<AnswerDto> answers;
}
