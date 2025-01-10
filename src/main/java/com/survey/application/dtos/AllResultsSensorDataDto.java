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
public class AllResultsSensorDataDto {
    @Schema(description = "Unique identifier for the sensor data.")
    private UUID sensorDataId;

    @Schema(description = "Date and time of the sensor data collection.")
    private OffsetDateTime dateTime;

    @Schema(description = "Temperature recorded by the sensor.")
    private BigDecimal temperature;

    @Schema(description = "Humidity recorded by the sensor.")
    private BigDecimal humidity;

    @Schema(description = "Unique identifier of the survey participation associated with this sensor data.")
    private UUID surveyParticipationId;
}
