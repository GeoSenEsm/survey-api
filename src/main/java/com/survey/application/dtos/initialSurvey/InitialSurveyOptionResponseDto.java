package com.survey.application.dtos.initialSurvey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class InitialSurveyOptionResponseDto {

    @Schema(description = "UUID of a question from initial survey.")
    private UUID id;

    @Schema(description = "Order in which the option will be displayed. Unique within the scope of the question.",
            example = "0",
            minimum = "0",
            maximum = "256")
    private Integer order;

    @Schema(description = "Option content.",
            minimum = "1",
            maximum = "250")
    private String content;
}
