package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNumberRangeOptionDto {

    @NotNull
    @Min(0)
    @Max(10)
    @Schema(description = "The starting value of number range in linear scale question.",
            example = "1")
    private Integer from;

    @NotNull
    @Min(0)
    @Max(10)
    @Schema(description = "The ending value of number range in linear scale question.",
            example = "5")
    private Integer to;

    @Size(max = 50)
    @Schema(description = "(optional) The label for the starting value of the range.",
            example = "Low")
    private String fromLabel;

    @Size(max = 50)
    @Schema(description = " (optional) The label for the ending value of the range.",
            example = "High")
    private String toLabel;
}
