package com.survey.application.dtos.surveyDtos;

import com.survey.domain.models.enums.SurveyState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseSurveyDto {

    @Schema(description = "UUID of the survey.")
    private UUID id;

    @Schema(description = "The title of the survey.",
            example = "Customer Satisfaction Survey",
            maximum = "100")
    private String name;

    @Schema(description = "State of the survey.",
            enumAsRef = true,
            allowableValues = {"created", "published"})
    private SurveyState state;

    @Schema(description = "A list of sections that make up the survey. The survey must contain at least one section.")
    private List<ResponseSurveySectionDto> sections;

    @Schema(example = "2001")
    private Long rowVersion;
}
