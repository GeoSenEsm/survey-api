package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class SurveyParticipationTimeStartFinishDto {
    @NotEmpty
    private OffsetDateTime start;
    @NotEmpty
    private OffsetDateTime finish;

}
