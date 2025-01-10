package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Schema(description = "Used to create a time slot for some survey. (Date and time range when a survey is available to respondents.)")
public class SurveyParticipationTimeStartFinishDto {

    @NotEmpty
    @Schema(description = "Date and time in UTC when given time slot is supposed to start.")
    private OffsetDateTime start;

    @NotEmpty
    @Schema(description = "Date and time in UTC when given time slot is supposed to end.")
    private OffsetDateTime finish;

}
