package com.survey.application.dtos.surveyDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSurveyDto {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private List<@Valid CreateSurveySectionDto> sections;
}
