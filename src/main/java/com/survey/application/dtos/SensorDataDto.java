package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorDataDto {

    @NotNull(message = "DateTime cannot be null!")
    @Schema(description = "Date and time in UTC when the sensor reading has been measured.")

    private OffsetDateTime dateTime;

    @NotBlank(message = "Source cannot be blank!")
    @Size(max = 32)
    @Schema(description = "Sensor source code, for example xiaomi, kestrel, or manual.")
    private String source;

    @NotEmpty(message = "Sensor values cannot be empty!")
    @Valid
    @Schema(description = "Captured values keyed by admin-defined parameter code.")
    private List<SensorDataValueDto> values;
}
