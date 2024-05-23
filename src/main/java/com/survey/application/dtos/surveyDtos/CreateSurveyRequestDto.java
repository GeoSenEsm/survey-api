package com.survey.application.dtos.surveyDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSurveyRequestDto {
    @NotNull
    private @Valid CreateSurveyDto survey;

    @NotNull
    private List<@Valid CreateSurveySectionDto> surveySection;
}
