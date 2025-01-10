package com.survey.application.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorDataDto {
    @NotNull(message = "DateTime cannot be null!")
    private OffsetDateTime dateTime;

    @NotNull(message = "Temperature cannot be null!")
    @DecimalMin(value = "-99.99", message = "Temperature must be grater than -99.99")
    @DecimalMax(value = "99.99", message = "Temperature must be less than 99.99")
    private BigDecimal temperature;

    @NotNull(message = "Humidity cannot be null!")
    @DecimalMin(value = "0.0", message = "Humidity must be grater than 0")
    @DecimalMax(value = "100.0", message = "Humidity must be less than or equal 100.0")
    private BigDecimal humidity;
}
