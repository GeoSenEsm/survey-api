package com.survey.application.dtos.initialSurvey;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInitialSurveyOptionDto {
    @NotNull
    private Integer order;
    @NotNull
    private String content;
}
