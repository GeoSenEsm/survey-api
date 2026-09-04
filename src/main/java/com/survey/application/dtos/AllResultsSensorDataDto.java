package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllResultsSensorDataDto {
    @Schema(description = "Unique identifier for the sensor data.")
    private UUID sensorDataId;

    @Schema(description = "Date and time of the sensor data collection.")
    private OffsetDateTime dateTime;

    @Schema(description = "Sensor source code, for example xiaomi, kestrel, or manual.")
    private String source;

    @Schema(description = "Captured values keyed by admin-defined parameter code.")
    private List<SensorDataValueDto> values;

    @Schema(description = "Unique identifier of the survey participation associated with this sensor data.")
    private UUID surveyParticipationId;
}
