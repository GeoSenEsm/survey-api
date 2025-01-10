package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Contains information about respondents participation in some survey.")
public class SurveyParticipationDto {

    @Schema(description = "UUID of survey participation.")
    private UUID id;

    @Schema(description = "ID of the respondent that this survey participation is about.")
    private UUID respondentId;

    @Schema(description = "ID of the survey that this survey participation is about.")
    private UUID surveyId;

    @Schema(description = "Date and time in UTC saved to the database when respondent submitted the survey response. This will be in fact either surveyStartDate or surveyFinishDate based on some conditions.")
    private OffsetDateTime date;

    @Schema(description = "Date and time in UTC when respondent started filling the survey.")
    private OffsetDateTime surveyStartDate;

    @Schema(description = "Date and time in UTC when respondent finished filling the survey.")
    private OffsetDateTime surveyFinishDate;

    @Schema(example = "2001")
    private Long rowVersion;
}
