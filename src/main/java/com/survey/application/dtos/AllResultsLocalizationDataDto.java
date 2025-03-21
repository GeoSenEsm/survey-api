package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllResultsLocalizationDataDto {
    @Schema(description = "Unique identifier for the localization data.")
    private UUID localizationDataId;

    @Schema(description = "Latitude of the localization point.")
    private BigDecimal latitude;

    @Schema(description = "Longitude of the localization point.")
    private BigDecimal longitude;

    @Schema(description = "Date and time of the localization data.")
    private OffsetDateTime dateTime;

    @Schema(description = "Indicates if the data point is outside the research area.")
    private Boolean outsideResearchArea;

    @Schema(description = "Unique identifier of the survey participation associated with this localization data.")
    private UUID surveyParticipationId;

    @Schema(description = "Accuracy of given geolocation measurement in meters.")
    private BigDecimal accuracyMeters;
}
