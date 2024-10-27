package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class RespondentDataAnswerDto {
    @NotNull
    private UUID questionId;
    @NotNull
    private String questionContent;
    @NotNull
    private UUID optionId;
}
