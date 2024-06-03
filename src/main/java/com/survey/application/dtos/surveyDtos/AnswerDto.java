package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AnswerDto {
    private UUID questionId;
    private List<SelectedOptionDto> selectedOptions;
    private int numericAnswer;
}
