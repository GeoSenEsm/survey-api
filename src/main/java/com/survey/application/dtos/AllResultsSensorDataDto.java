package com.survey.application.dtos;

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
    private UUID sensorDataId;
    private OffsetDateTime dateTime;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private UUID surveyParticipationId;
}
