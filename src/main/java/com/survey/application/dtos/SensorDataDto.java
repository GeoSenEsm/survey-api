package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class SensorDataDto {

    @NotNull(message = "DateTime cannot be null!")
    @Schema(description = "Date and time in UTC when the sensor reading has been measured.")

    private OffsetDateTime dateTime;

    @NotNull(message = "Temperature cannot be null!")
    @DecimalMin(value = "-99.99", message = "Temperature must be grater than -99.99")
    @DecimalMax(value = "99.99", message = "Temperature must be less than 99.99")
    @Schema(description = "Temperature in Celsius. Precision up to 2 decimal places.",
            example = "21.57")
    private BigDecimal temperature;

    @NotNull(message = "Humidity cannot be null!")
    @DecimalMin(value = "0.0", message = "Humidity must be grater than 0")
    @DecimalMax(value = "100.0", message = "Humidity must be less than or equal 100.0")
    @Schema(description = "Humidity in percents. Precision up to 2 decimal places.",
            example = "45.21")
    private BigDecimal humidity;
}
