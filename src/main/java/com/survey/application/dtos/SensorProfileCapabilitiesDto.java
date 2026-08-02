package com.survey.application.dtos;

import java.util.List;

public record SensorProfileCapabilitiesDto(
        List<Integer> supportedSchemaVersions,
        String currentEngineVersion,
        List<String> supportedAdapterKeys,
        List<String> supportedTransports,
        List<String> supportedGattOperations,
        List<String> supportedAdvertisementDecoders
) {}
