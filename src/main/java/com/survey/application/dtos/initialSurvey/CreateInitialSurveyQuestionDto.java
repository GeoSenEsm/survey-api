package com.survey.application.dtos.initialSurvey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInitialSurveyQuestionDto {

    @NotNull
    @Min(0)
    @Max(256)
    @Schema(description = "Order in which question will be displayed in the initial survey. Unique within the scope of the initial survey.",
            example = "0")
    private Integer order;

    @NotBlank
    @Size(max = 250)
    @Schema(description = "Question content.",
            example = "Gender")
    private String content;

    @NotEmpty
    private List<@Valid CreateInitialSurveyOptionDto> options;
}
