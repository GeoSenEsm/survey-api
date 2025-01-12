package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LocalizationPointDto {

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

    @Schema(description = "Date and time of given geolocation reading in UTC.")
    private OffsetDateTime dateTime;
    private Boolean outsideResearchArea;
}
