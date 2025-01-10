package com.survey.application.dtos.initialSurvey;

import com.survey.domain.models.enums.SurveyState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitialSurveyStateDto {
    @Schema(description = "State of initial survey.",
            enumAsRef = true,
            allowableValues = {"not_created", "created", "published"})
    private String text;
}
