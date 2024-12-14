package com.survey.application.services;

import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SensorDataService {
    List<ResponseSensorDataDto> saveSensorData(List<SensorDataDto> temperatureDataDtoList);
    List<ResponseSensorDataDto> getSensorData(OffsetDateTime from, OffsetDateTime to);
    LastSensorEntryDateDto getDateOfLastSensorDataForRespondent(UUID respondentId);
}
