package com.survey.application.dtos.initialSurvey;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class InitialSurveyQuestionResponseDto {
    private UUID id;
    private Integer order;
    private String content;
    private List<InitialSurveyOptionResponseDto> options;
}
