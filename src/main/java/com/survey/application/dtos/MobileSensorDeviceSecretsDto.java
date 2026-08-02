package com.survey.application.dtos;

import java.util.Map;
import java.util.UUID;

public record MobileSensorDeviceSecretsDto(
        UUID sensorMacId,
        Map<String, String> secrets
) {}
