package com.survey.application.dtos.surveyDtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SendOfflineSurveyResponseDto implements SendSurveyResponseDto {
    @NotNull
    private UUID surveyId;
    @NotNull
    private OffsetDateTime startDate;
    @NotNull
    private OffsetDateTime finishDate;
    @NotNull
    private List<AnswerDto> answers;
}
