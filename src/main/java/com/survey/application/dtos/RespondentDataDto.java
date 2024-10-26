package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RespondentDataDto {
    private UUID respondentId;
    private UUID identityUserId;
    private String username;
    private UUID surveyId;
    private List<RespondentDataAnswerDto> answers;
}
