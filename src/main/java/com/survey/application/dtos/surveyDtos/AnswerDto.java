package com.survey.application.dtos.surveyDtos;

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
    private UUID questionId;
    private List<SelectedOptionDto> selectedOptions;
    private Integer numericAnswer;
    private Boolean yesNoAnswer;
    private String textAnswer;
}
