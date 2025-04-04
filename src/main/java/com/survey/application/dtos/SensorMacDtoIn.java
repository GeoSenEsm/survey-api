package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorMacDtoIn {
    @NotNull
    @Size(min = 1, max = 16)
    @Schema(description = "Sensor ID - customizable.",
            example = "Sensor123")
    private String sensorId;

    @NotNull
    @Pattern(regexp = "^([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}$")
    @Schema(description = "MAC address of the sensor, formatted as six pairs of hexadecimal characters separated by colons. Letters can be either uppercase or lowercase. They will be converted to uppercase for database storage.",
            example = "00:1A:2B:3C:4D:5E")
    private String sensorMac;
}
