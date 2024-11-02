package com.survey.application.services;

import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface SensorDataService {
    List<ResponseSensorDataDto> saveSensorData(String token, List<SensorDataDto> temperatureDataDtoList);
    List<ResponseSensorDataDto> getSensorData(OffsetDateTime from, OffsetDateTime to);
}
