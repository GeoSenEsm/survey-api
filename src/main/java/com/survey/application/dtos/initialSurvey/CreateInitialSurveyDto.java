package com.survey.application.dtos.initialSurvey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CreateInitialSurveyDto {
    @NotEmpty
    private List<@Valid CreateInitialSurveyQuestionDto> questions;
}
