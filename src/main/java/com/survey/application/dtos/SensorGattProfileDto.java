package com.survey.application.dtos;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SensorGattProfileDto(
        UUID id,
        UUID sensorTypeId,
        String sensorTypeCode,
        int revision,
        String status,
        int schemaVersion,
        JsonNode spec,
        String specHash,
        String minEngineVersion,
        boolean readOnly,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime publishedAt,
        byte[] rowVersion
) {}
