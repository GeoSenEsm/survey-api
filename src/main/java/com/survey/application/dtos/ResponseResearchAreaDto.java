package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class ResponseResearchAreaDto {

    @Schema(description = "UUID of localization data point.")
    private UUID id;

    @Schema(description = "Latitude of a point creating research area polygon. Precision up to 6 decimal places.",
            example = "52.228851",
            minimum = "-90.0",
            maximum = "90.0")
    private BigDecimal latitude;

    @Schema(description = "Longitude of a point creating research area polygon. Precision up to 6 decimal places.",
            example = "21.020921",
            minimum = "-180.0",
            maximum = "180.0")
    private BigDecimal longitude;

    @Schema(example = "2001")
    private Long rowVersion;
}
