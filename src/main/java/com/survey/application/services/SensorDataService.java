package com.survey.application.services;

import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;

import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SensorDataService {
    List<ResponseSensorDataDto> saveSensorData(List<SensorDataDto> temperatureDataDtoList);
    List<ResponseSensorDataDto> getSensorData(OffsetDateTime from, OffsetDateTime to, UUID identityUserId);
    List<ResponseSensorDataDto> getSensorDataBatch(OffsetDateTime from, OffsetDateTime to, UUID identityUserId, int offset, int limit);
    void streamSensorData(OutputStream outputStream, OffsetDateTime from, OffsetDateTime to, UUID identityUserId) throws Exception;
    LastSensorEntryDateDto getDateOfLastSensorDataForRespondent(UUID identityUserId);
}
