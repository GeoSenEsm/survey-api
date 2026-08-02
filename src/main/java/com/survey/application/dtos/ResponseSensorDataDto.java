package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseSensorDataDto {

    @Schema(description = "UUID of given sensor reading.")
    private UUID id;

    @Schema(description = "UUID of the respondent that submitted this sensor reading.")
    private UUID respondentId;

    @Schema(description = "Date and time in UTC when the sensor reading has been measured.")
    private OffsetDateTime dateTime;

    @Schema(description = "Sensor source code, for example xiaomi, kestrel, or manual.")
    private String source;

    @Schema(description = "Captured values keyed by admin-defined parameter code.")
    private List<SensorDataValueDto> values;
}
