package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateRespondentDataDto {

    @NotNull
    @Schema(description = "UUID of question from initial survey.")
    private UUID questionId;

    @NotNull
    @Schema(description = "UUID of option from initial survey question that the respondent selected.")
    private UUID optionId;
}
