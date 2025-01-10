package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllResultsSurveyParticipationDto {
    @Schema(description = "Unique identifier for the survey participation.")
    private UUID surveyParticipationId;

    @Schema(description = "Unique identifier for the survey.")
    private UUID surveyId;

    @Schema(description = "Name of the survey.")
    private String surveyName;

    @Schema(description = "Date and time of the survey response.")
    private OffsetDateTime responseDate;

    @Schema(description = "List of questions and answers associated with the survey participation.")
    private List<AllResultsQuestionAnswerDto> questionAnswerList;
}
