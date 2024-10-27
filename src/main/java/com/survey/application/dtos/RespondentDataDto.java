package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RespondentDataDto {
    private UUID id;
    private UUID identityUserId;
    private String username;
    private List<RespondentDataAnswerDto> answers;
}
