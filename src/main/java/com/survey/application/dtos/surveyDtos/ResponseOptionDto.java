package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseOptionDto {

    @Schema(description = "UUID of the option.")
    private UUID id;

    @Schema(description = "The order in which the option will be displayed. Unique within the scope of the question.",
            example = "1",
            minimum = "1",
            maximum = "9999")
    private Integer order;

    @Schema(description = "Option content.",
            example = "Option content",
            minimum = "1",
            maximum = "150")
    private String label;

    @Schema(description = "(optional) Order of the section that will be shown after selecting this option. Section in question must have visibility set as `answer_triggered`.",
            minimum = "1",
            maximum = "9999")
    private Integer showSection;

    @Schema(description = "(optional) The file path to an image associated with the option.",
            example = "/images/option_image.jpg")
    private String imagePath;

    private Long rowVersion;
}
