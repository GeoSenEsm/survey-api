package com.survey.application.services;

import com.survey.application.dtos.AssignSensorRespondentDto;
import com.survey.application.dtos.UpdatedSensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;
import com.survey.application.dtos.SensorTypeDtoOut;

import java.util.List;
import java.util.Optional;

public interface SensorMacService {
    List<SensorMacDtoOut> saveSensorMacList(List<SensorMacDtoIn> dtoList);
    void deleteSensorMac(String sensorId);
    void deleteAll();
    SensorMacDtoOut updateSensorMacBySensorId(String sensorId, UpdatedSensorMacDtoIn updatedSensorMacDtoIn);
    SensorMacDtoOut assignRespondent(String sensorId, AssignSensorRespondentDto dto);
    List<SensorMacDtoOut> getFullSensorMacList();
    SensorMacDtoOut getSensorMacBySensorId(String sensorId);
    Optional<SensorMacDtoOut> getAssignedToCurrentRespondent();
    List<SensorTypeDtoOut> getSensorTypes();
}
