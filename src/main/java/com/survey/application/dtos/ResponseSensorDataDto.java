package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ResponseSensorDataDto {

    @Schema(description = "UUID of given sensor reading.")
    private UUID id;

    @Schema(description = "UUID of the respondent that submitted this sensor reading.")
    private UUID respondentId;

    @Schema(description = "Date and time in UTC when the sensor reading has been measured.")
    private OffsetDateTime dateTime;

    @Schema(description = "Temperature in Celsius. Precision up to 2 decimal places.",
            example = "21.57",
            minimum = "-99.99",
            maximum = "99.99")
    private BigDecimal temperature;

    @Schema(description = "Humidity in percents. Precision up to 2 decimal places.",
            example = "45.21",
            minimum = "0.00",
            maximum = "100.00")
    private BigDecimal humidity;
}
