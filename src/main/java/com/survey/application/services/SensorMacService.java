package com.survey.application.services;

import com.survey.application.dtos.UpdatedSensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;

import java.util.List;

public interface SensorMacService {
    List<SensorMacDtoOut> saveSensorMacList(List<SensorMacDtoIn> dtoList);
    void deleteSensorMac(String sensorId);
    void deleteAll();
    SensorMacDtoOut updateSensorMacBySensorId(String sensorId, UpdatedSensorMacDtoIn updatedSensorMacDtoIn);
    List<SensorMacDtoOut> getFullSensorMacList();
    SensorMacDtoOut getSensorMacBySensorId(String sensorId);
}
