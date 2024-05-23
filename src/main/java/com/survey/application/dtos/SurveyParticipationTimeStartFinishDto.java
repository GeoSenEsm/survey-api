package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class SurveyParticipationTimeStartFinishDto {
    @NotNull
    private OffsetDateTime start;
    @NotNull
    private OffsetDateTime finish;

}
