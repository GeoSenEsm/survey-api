package com.survey.application.dtos;

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
    private UUID identityUserId;
    private String username;
    private List<AllResultsSurveyParticipationDto> surveyResults;
    private List<AllResultsLocalizationDataDto> localizationDataList;
    private List<AllResultsSensorDataDto> sensorDataList;
}
