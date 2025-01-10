package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

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

    @Schema(example = "2001")
    private Long rowVersion;
}
