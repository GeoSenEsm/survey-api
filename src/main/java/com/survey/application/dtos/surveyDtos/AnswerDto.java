package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AnswerDto {

    @NotNull
    @Schema(description = "UUID of the question this answer is referring to.")
    private UUID questionId;

    @Schema(description = "(optional) List of options selected by the respondent in the given question. Required only if the question type is `single_choice` or `multiple_choice`.")
    private List<SelectedOptionDto> selectedOptions;

    @Schema(description = "(optional) Numeric answer provided by the respondent. Required only if question type is `number_input` or `linear_scale`.",
            example = "5")
    private Integer numericAnswer;

    @Schema(description = "(optional) Yes/No answer provided by the respondent. Required only if question type is `yes_no_choice`.",
            example = "true")
    private Boolean yesNoAnswer;

    @Schema(description = "(optional) Textual answer provided by the respondent. Required only if question type is `text_input`.",
            example = "This is a text answer.")
    private String textAnswer;
}
