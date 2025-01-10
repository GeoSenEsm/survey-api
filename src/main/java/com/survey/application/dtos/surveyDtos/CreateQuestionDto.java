package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.ValidQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateQuestionDto {

    @NotNull
    @Min(1)
    @Max(9999)
    @Schema(description = "The order in which the question will be displayed. Unique within the scope of the survey.",
            example = "1")
    private Integer order;

    @NotBlank
    @Size(min = 1, max = 250)
    @Schema(description = "The content of the question.",
            example = "What is your opinion on the product?")
    private String content;

    @NotNull
    @ValidQuestionType
    @Schema(description = "The type of question. Valid values are predefined and validated through custom validation annotations.",
            enumAsRef = true,
            allowableValues = {"single_choice", "linear_scale", "yes_no_choice", "multiple_choice", "number_input", "image_choice", "text_input"})
    private String questionType;

    @NotNull
    @Schema(description = "Indicates whether answering the question is required for the respondent. Set to 'true' for mandatory questions.",
            example = "true")
    private boolean required;

    @Schema(description = "(optional) Must be set when question type is `single_choice` or `multiple_choice`.")
    private List<@Valid CreateOptionDto> options;

    @Valid
    @Schema(description = "Must be set when question type is `linear_scale`.",
            implementation = CreateNumberRangeOptionDto.class)
    private CreateNumberRangeOptionDto numberRange;

}
