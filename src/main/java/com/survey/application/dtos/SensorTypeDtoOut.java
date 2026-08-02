package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SensorTypeDtoOut {

    @Schema(description = "UUID of the sensor type.")
    private UUID id;

    @Schema(description = "Stable machine code used by clients.", example = "xiaomi")
    private String code;

    @Schema(description = "Admin-facing display name.", example = "Xiaomi")
    private String name;
}
