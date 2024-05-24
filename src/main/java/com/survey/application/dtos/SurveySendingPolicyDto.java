package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SurveySendingPolicyDto {
    private UUID id;
    private UUID surveyId;
    private Long rowVersion;

}
