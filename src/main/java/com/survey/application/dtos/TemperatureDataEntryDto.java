package com.survey.application.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class TemperatureDataEntryDto {
    @NotNull(message = "DateTime cannot be null!")
    private OffsetDateTime dateTime;

    @NotNull(message = "Temperature cannot be null!")
    @DecimalMin(value = "-99.99", message = "Temperature must be grater than -99.99")
    @DecimalMax(value = "99.99", message = "Temperature must be less than 99.99")
    private BigDecimal temperature;
}
