package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class SurveyParticipationDto {
    private UUID id;
    private UUID respondentId;
    private UUID surveyId;
    private OffsetDateTime date;
    private Long rowVersion;
}
