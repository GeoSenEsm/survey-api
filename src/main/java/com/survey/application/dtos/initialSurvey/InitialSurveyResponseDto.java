package com.survey.application.dtos.initialSurvey;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class InitialSurveyResponseDto {
    private UUID id;
    private List<InitialSurveyQuestionResponseDto> questions;
}
