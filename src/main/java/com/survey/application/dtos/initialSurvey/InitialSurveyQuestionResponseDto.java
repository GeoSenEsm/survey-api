package com.survey.application.dtos.initialSurvey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class InitialSurveyQuestionResponseDto {

    @Schema(description = "UUID of a question in initial survey.")
    private UUID id;

    @Schema(description = "Order in which question will be displayed in the initial survey. Unique within the scope of the initial survey.",
            example = "0",
            minimum = "0",
            maximum = "256")
    private Integer order;

    @Schema(description = "Question content.",
            example = "Gender",
            minimum = "1",
            maximum = "250")
    private String content;

    private List<InitialSurveyOptionResponseDto> options;
}
