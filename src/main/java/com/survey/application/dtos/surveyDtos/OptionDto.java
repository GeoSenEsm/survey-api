package com.survey.application.dtos.surveyDtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionDto {

    @NotNull
    @Min(1)
    @Max(9999)
    private Integer order;

    @NotBlank
    @Size(min = 1, max = 50)
    private String label;
}
