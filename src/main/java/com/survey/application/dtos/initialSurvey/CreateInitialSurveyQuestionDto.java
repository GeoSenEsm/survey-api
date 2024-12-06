package com.survey.application.dtos.initialSurvey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInitialSurveyQuestionDto {
    @NotNull
    private Integer order;
    @NotNull
    private String content;
    @NotEmpty
    private List<@Valid CreateInitialSurveyOptionDto> options;
}
