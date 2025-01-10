package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOptionDto {

    @NotNull
    @Min(1)
    @Max(9999)
    @Schema(description = "The order in which the option will be displayed. Unique within the scope of the question.",
            example = "1")
    private Integer order;

    @NotBlank
    @Size(min = 1, max = 150)
    @Schema(description = "Option content.",
            example = "Option content")
    private String label;

    @Min(1)
    @Max(9999)
    @Schema(description = "(optional) Order of the section that will be shown after selecting this option. Section in question must have visibility set as `answer_triggered`.")
    private Integer showSection;

    @Schema(description = "(optional) The file path to an image associated with the option.",
            example = "/images/option_image.jpg")
    private String imagePath;
}
