package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllResultsDto {
    @Schema(description = "Unique identifier for the respondent.")
    private UUID respondentId;

    @Schema(description = "Username of the respondent.")
    private String username;

    @Schema(description = "List of survey participation results for the respondent.")
    private List<AllResultsSurveyParticipationDto> surveyResults;

    @Schema(description = "List of localization data for the respondent.")
    private List<AllResultsLocalizationDataDto> localizationDataList;

    @Schema(description = "List of sensor data collected for the respondent.")
    private List<AllResultsSensorDataDto> sensorDataList;
}
