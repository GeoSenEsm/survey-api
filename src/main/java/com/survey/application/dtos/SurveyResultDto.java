package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SurveyResultDto {
    private String surveyName;
    private String question;
    private OffsetDateTime responseDate;
    private List<Object> answers;
    private List<LocalizationPointDto> localizations;
    private UUID respondentId;
}
