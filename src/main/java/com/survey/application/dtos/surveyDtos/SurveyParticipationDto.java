package com.survey.application.dtos.surveyDtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class SurveyParticipationDto {
    private UUID id;
    private UUID respondentId;
    private UUID surveyId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date date;
    private Long rowVersion;
}
