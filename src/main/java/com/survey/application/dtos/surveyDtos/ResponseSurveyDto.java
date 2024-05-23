package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
public class ResponseSurveyDto {
    private UUID id;
    private String name;
    private Long rowVersion;
}
