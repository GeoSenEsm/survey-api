package com.survey.application.services;

import com.survey.application.dtos.GattProfileValidationDto;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorGattProfileWriteDto;
import com.survey.application.dtos.SensorProfileCapabilitiesDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SensorGattProfileService {
    SensorProfileCapabilitiesDto capabilities();
    List<SensorGattProfileDto> listRevisions(UUID sensorTypeId);
    SensorGattProfileDto get(UUID profileId);
    SensorGattProfileDto createDraft(UUID sensorTypeId, SensorGattProfileWriteDto dto);
    SensorGattProfileDto updateDraft(UUID profileId, SensorGattProfileWriteDto dto);
    GattProfileValidationDto validate(UUID profileId);
    SensorGattProfileDto publish(UUID profileId);
    SensorGattProfileDto rollback(UUID sensorTypeId, int revision);
    SensorTypeDtoOut createSensorType(SensorTypeCreateDto dto);
    List<SensorGattProfileDto> getPublishedProfiles(Set<UUID> sensorTypeIds);
    List<JsonNode> getPublishedProfilesForMobile(Set<UUID> sensorTypeIds);
    List<SensorTypeDtoOut> listProfileSensorTypes();
}
