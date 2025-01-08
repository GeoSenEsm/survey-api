package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateSurveySendingPolicyDto {

    @NotNull
    @Schema(description = "UUID of the survey to which sending policy is being created.")
    private UUID surveyId;

    @NotEmpty
    @Schema(description = "List of time slots when survey will be active for respondents.")
    private List<SurveyParticipationTimeStartFinishDto> surveyParticipationTimeSlots;

}
