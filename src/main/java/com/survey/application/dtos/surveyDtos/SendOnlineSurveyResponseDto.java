package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.ValidSendSurveyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ValidSendSurveyResponse
@Accessors(chain = true)
public class SendOnlineSurveyResponseDto implements SendSurveyResponseDto {
    @NotNull
    private UUID surveyId;

    @NotNull
    @Schema(description = "UTC date and time when respondent started filling the survey.")
    private OffsetDateTime startDate;

    @NotNull
    @Schema(description = "UTC date and time when respondent finished filling the survey.")
    private OffsetDateTime finishDate;

    @NotNull
    private List<AnswerDto> answers;

}
