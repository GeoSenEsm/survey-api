package com.survey.application.dtos.surveyDtos;

import com.survey.domain.models.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseQuestionDto {

    @Schema(description = "UUID of the question.")
    private UUID id;

    @Schema(description = "The order in which the question will be displayed. Unique within the scope of the survey.",
            example = "1",
            minimum = "1",
            maximum = "9999")
    private Integer order;

    @Schema(description = "The content of the question.",
            example = "What is your opinion on the product?",
            minimum = "1",
            maximum = "250")
    private String content;

    @Schema(description = "The type of question. Valid values are predefined.",
            enumAsRef = true,
            allowableValues = {"single_choice", "linear_scale", "yes_no_choice", "multiple_choice", "number_input", "image_choice", "text_input"})
    private QuestionType questionType;

    @Schema(description = "Indicates whether answering the question is required for the respondent.",
            example = "true")
    private boolean required;

    private Long rowVersion;

    @Schema(description = "(optional) Present only when question type is `single_choice` or `multiple_choice`.")
    private List<ResponseOptionDto> options;

    @Schema(description = "(optional) Present only when question type is `linear_scale`")
    private ResponseNumberRangeOptionDto numberRange;
}
