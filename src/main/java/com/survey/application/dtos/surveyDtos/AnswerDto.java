package com.survey.application.dtos.surveyDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnswerDto {
    private UUID questionId;
    private List<SelectedOptionDto> selectedOptions;
    private Integer numericAnswer;
    private Boolean yesNoAnswer;
}
