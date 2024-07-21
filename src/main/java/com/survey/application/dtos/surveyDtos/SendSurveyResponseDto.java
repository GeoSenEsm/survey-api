package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.ValidSendSurveyResponse;
import jakarta.validation.constraints.NotNull;
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

@ValidSendSurveyResponse
public class SendSurveyResponseDto {
    @NotNull
    private UUID surveyId;
    @NotNull
    private List<AnswerDto> answers;
}
