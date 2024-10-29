package com.survey.application.dtos.initialSurvey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CreateInitialSurveyQuestionDto {
    @NotNull
    private Integer order;
    @NotNull
    private String content;
    @NotEmpty
    private List<@Valid CreateInitialSurveyOptionDto> options;
}
