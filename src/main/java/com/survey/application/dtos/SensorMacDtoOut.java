package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SensorMacDtoOut {
    private UUID id;
    private String sensorId;
    private String sensorMac;
    private Long rowVersion;
}
