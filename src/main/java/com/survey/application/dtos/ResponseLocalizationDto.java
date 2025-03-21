package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ResponseLocalizationDto {

    @Schema(description = "UUID of geolocation point saved in database.")
    private UUID id;

    @Schema(description = "UUID of the respondent that sent this geolocation data.")
    private UUID respondentId;

    @Schema(description = "(optional) UUID of the survey participation during witch the geolocation was saved.")
    private UUID surveyParticipationId;

    @Schema(description = "(optional) UUID of the survey during witch the geolocation was saved.")
    private UUID surveyId;

    @Schema(description = "Date and time in UTC when given localization data was measured.")
    private OffsetDateTime dateTime;

    @Schema(description = "Precision up to 6 decimal places.",
            example = "52.228851",
            minimum = "-90.0",
            maximum = "90.0")
    private BigDecimal latitude;

    @Schema(description = "Precision up to 6 decimal places.",
            example = "21.020921",
            minimum = "-180.0",
            maximum = "180.0")
    private BigDecimal longitude;

    @Schema(description = "Is given geolocation point outside defined research area. Null if research area is not defined.")
    private Boolean outsideResearchArea;

    @Schema(example = "2001")
    private Long rowVersion;

    @Schema(description = "Accuracy of location measurement in meters.",
            example = "20.50",
            minimum = "0.0",
            maximum = "999999.99")
    private BigDecimal accuracyMeters;
}
