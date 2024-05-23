package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class ResponseSurveyRequestDto {
    private ResponseSurveyDto survey;

    private List<ResponseSurveySectionDto> surveySection;
}
