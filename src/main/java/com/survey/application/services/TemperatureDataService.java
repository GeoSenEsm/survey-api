package com.survey.application.services;

import com.survey.application.dtos.ResponseTemperatureDataEntryDto;
import com.survey.application.dtos.TemperatureDataEntryDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface TemperatureDataService {
    List<ResponseTemperatureDataEntryDto> saveTemperatureData(String token, List<TemperatureDataEntryDto> temperatureDataDtoList);
    List<ResponseTemperatureDataEntryDto> getTemperatureData(OffsetDateTime from, OffsetDateTime to);
}
