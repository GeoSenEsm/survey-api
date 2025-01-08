package com.survey.application.dtos.initialSurvey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInitialSurveyOptionDto {

    @NotNull
    @Min(0)
    @Max(256)
    @Schema(description = "Order in which the option will be displayed. Unique within the scope of the question.",
            example = "0")
    private Integer order;

    @NotBlank
    @Size(max = 250)
    @Schema(description = "Option content.",
            example = "male")
    private String content;
}
