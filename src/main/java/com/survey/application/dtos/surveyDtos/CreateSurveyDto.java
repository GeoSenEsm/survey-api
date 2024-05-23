package com.survey.application.dtos.surveyDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSurveyDto {

    @NotBlank
    @Size(max = 100)
    private String name;
}
