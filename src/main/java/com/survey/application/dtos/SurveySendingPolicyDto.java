package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SurveySendingPolicyDto {
    @NotNull
    private UUID id;
    private UUID surveyId;
    private Long rowVersion;

}
