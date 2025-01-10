package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllResultsQuestionAnswerDto {
    @Schema(description = "The text of the question.")
    private String question;

    @Schema(description = "List of answers to the question. Can contain multiple response types.")
    private List<Object> answers;
}
