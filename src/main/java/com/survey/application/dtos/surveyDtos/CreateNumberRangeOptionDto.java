package com.survey.application.dtos.surveyDtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNumberRangeOptionDto {

    @NotNull
    @Min(0)
    @Max(10)
    private Integer from;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer to;

    @Size(max = 50)
    private String startLabel;

    @Size(max = 50)
    private String endLabel;
}
