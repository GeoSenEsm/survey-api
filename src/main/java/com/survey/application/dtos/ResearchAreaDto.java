package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ResearchAreaDto {
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Digits(integer = 2, fraction = 6)
    @Schema(description = "Latitude of a point creating research area polygon. Precision up to 6 decimal places.",
            example = "52.228851",
            minimum = "-90.0",
            maximum = "90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Digits(integer = 3, fraction = 6)
    @Schema(description = "Longitude of a point creating research area polygon. Precision up to 6 decimal places.",
            example = "21.020921",
            minimum = "-180.0",
            maximum = "180.0")
    private BigDecimal longitude;
}
