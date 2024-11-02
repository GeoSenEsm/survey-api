package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ResponseSensorDataDto {
    private UUID id;
    private UUID respondentId;
    private OffsetDateTime dateTime;
    private BigDecimal temperature;
    private BigDecimal humidity;
}
