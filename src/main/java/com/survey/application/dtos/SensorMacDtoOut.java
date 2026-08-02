package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SensorMacDtoOut {

    @Schema(description = "UUID of the database row.")
    private UUID id;

    @Schema(description = "Sensor ID - customizable.",
            example = "Sensor123",
            minimum = "1",
            maximum = "16")
    private String sensorId;

    @Schema(description = "MAC address of the sensor, formatted as six pairs of hexadecimal characters separated by colons. Letters can be either uppercase or lowercase. They will be converted to uppercase for database storage.",
            example = "00:1A:2B:3C:4D:5E")
    private String sensorMac;

    @Schema(description = "Assigned respondent identity_user id, if any.", nullable = true)
    private UUID respondentId;

    @Schema(description = "Assigned respondent username, if any.", nullable = true)
    private String respondentUsername;

    @Schema(description = "Sensor type id.")
    private UUID sensorTypeId;

    @Schema(description = "Stable sensor type code (xiaomi, kestrel, manual, none).", example = "xiaomi")
    private String sensorTypeCode;

    @Schema(description = "Sensor type display name.", example = "Xiaomi")
    private String sensorTypeName;

    @Schema(description = "Names of device secrets configured for this sensor. Secret values are never returned.")
    private List<String> configuredSecrets;

    @Schema(example = "2001")
    private Long rowVersion;
}
