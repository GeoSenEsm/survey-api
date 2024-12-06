package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateRespondentDataDto {
    @NotNull
    private UUID questionId;
    @NotNull
    private UUID optionId;
}
