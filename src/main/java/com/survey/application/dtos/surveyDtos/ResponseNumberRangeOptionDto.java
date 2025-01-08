package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseNumberRangeOptionDto {

    @Schema(description = "UUID of number range.")
    private UUID id;

    @Schema(description = "The starting value of number range in linear scale question.",
            example = "1",
            minimum = "0",
            maximum = "10")
    private Integer from;

    @Schema(description = "The ending value of number range in linear scale question.",
            example = "5",
            minimum = "0",
            maximum = "10")
    private Integer to;

    @Schema(description = "(optional) The label for the starting value of the range.",
            example = "Low",
            maximum = "50")
    private String fromLabel;

    @Schema(description = "(optional) The label for the ending value of the range.",
            example = "High",
            maximum = "50")
    private String toLabel;

    private Long rowVersion;
}
