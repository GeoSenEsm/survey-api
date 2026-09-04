package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.ValidSendSurveyResponse;
import com.survey.application.dtos.SensorDataDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
    @Schema(description = "Date and time when respondent started filling the survey. May use the respondent's local offset; stored as UTC with denormalized local_date/local_time.")
    private OffsetDateTime startDate;

    @NotNull
    @Schema(description = "Date and time when respondent finished filling the survey. May use the respondent's local offset; validated against study wall-clock slots in the respondent timezone.")
    private OffsetDateTime finishDate;

    @Valid
    private List<SensorDataDto> sensorData;

    @NotNull
    private List<AnswerDto> answers;

}
