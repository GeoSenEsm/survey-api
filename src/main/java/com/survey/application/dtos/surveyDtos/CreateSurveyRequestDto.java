package com.survey.application.dtos.surveyDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSurveyRequestDto {
    @NotNull
    private @Valid SurveyDto survey;

    @NotNull
    private List<@Valid SurveySectionDto> surveySection;
}
