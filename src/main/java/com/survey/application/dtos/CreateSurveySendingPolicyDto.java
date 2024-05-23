package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateSurveySendingPolicyDto {
    @NotNull
    private UUID surveyId;

    @NotNull
    private List<SurveyParticipationTimeStartFinishDto> surveyParticipationTimeSlots;

}
