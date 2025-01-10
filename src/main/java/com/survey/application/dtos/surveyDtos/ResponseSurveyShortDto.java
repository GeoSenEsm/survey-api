package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Brief information about a survey. Contains only its UUID and title.")
public class ResponseSurveyShortDto {
    private UUID id;
    private String name;
}
