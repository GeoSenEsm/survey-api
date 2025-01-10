package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Self explanatory.")
public class SurveyResultDto {
    private String surveyName;
    private String question;
    private OffsetDateTime responseDate;
    private List<Object> answers;
    private LocalizationPointDto localizationData;
    private SensorDataDto sensorData;
    private UUID respondentId;
}
