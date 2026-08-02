package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
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

    @Schema(description = "Client integration strategy.")
    private String integrationMode;

    @Schema(description = "Optional native adapter identifier.")
    private String adapterKey;

    @Schema(description = "Secret names (e.g. bind_key) the type's published profile requires.")
    private List<String> requiredSecrets = List.of();
}
