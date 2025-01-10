package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SurveySendingPolicyDto {
    @Schema(description = "UUID of given survey sending policy.")
    private UUID id;

    @Schema(description = "UUID of the survey that this policy refers to.")
    private UUID surveyId;

    private List<SurveySendingPolicyTimesDto> timeSlots;

    @Schema(example = "2001")
    private Long rowVersion;
}
