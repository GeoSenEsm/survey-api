package com.survey.application.dtos.surveyDtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOptionDto {

    @NotNull
    @Min(1)
    @Max(9999)
    private Integer order;

    @NotBlank
    @Size(min = 1, max = 150)
    private String label;

    @Min(1)
    @Max(9999)
    private Integer showSection;

    private String imagePath;
}
