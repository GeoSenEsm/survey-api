package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorDataValueDto {
    @NotBlank
    @Size(max = 64)
    @Schema(description = "Admin-defined sensor parameter code.", example = "temperature")
    private String parameterCode;

    @NotBlank
    @Size(max = 256)
    @Schema(description = "Captured sensor value as text. The parameter definition declares how to interpret it.")
    private String value;
}
