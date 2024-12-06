package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseSurveyShortDto {
    private UUID id;
    private String name;
}
