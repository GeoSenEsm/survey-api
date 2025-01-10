package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "The title of the survey.",
            example = "Customer Satisfaction Survey")
    private String name;

    @NotNull
    private List<@Valid CreateSurveySectionDto> sections;
}
