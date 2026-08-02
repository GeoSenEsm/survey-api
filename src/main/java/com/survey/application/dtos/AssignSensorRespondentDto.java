package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignSensorRespondentDto {

    @Schema(description = "Respondent (identity_user) to assign this sensor to. Null clears the assignment.",
            nullable = true)
    private UUID respondentId;
}
