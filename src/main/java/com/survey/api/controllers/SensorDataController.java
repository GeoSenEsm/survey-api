package com.survey.api.controllers;

import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.application.services.SensorDataService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/sensordata")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @Autowired
    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @PostMapping
    @CrossOrigin
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ResponseSensorDataDto>> saveSensorData(
            @Valid @RequestBody List<SensorDataDto> temperatureDataDtoList,
            @RequestHeader(value = "Authorization", required = false) String token){

        List<ResponseSensorDataDto> savedTemperatureData = sensorDataService.saveSensorData(token, temperatureDataDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTemperatureData);
    }

    @GetMapping
    @CrossOrigin
    public ResponseEntity<List<ResponseSensorDataDto>> getSensorData(
            @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to){

        List<ResponseSensorDataDto> dtos = sensorDataService.getSensorData(from, to);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }


}
