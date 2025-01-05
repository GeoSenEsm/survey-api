package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatedSensorMacDtoIn {

    @NotNull
    @Pattern(regexp = "^([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}$")
    private String sensorMac;
}
