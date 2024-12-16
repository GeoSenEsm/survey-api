package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SensorDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/sensordata")
public class SensorDataController {

    private final SensorDataService sensorDataService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SensorDataController(SensorDataService sensorDataService, ClaimsPrincipalService claimsPrincipalService) {
        this.sensorDataService = sensorDataService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    public ResponseEntity<List<ResponseSensorDataDto>> saveSensorData(
            @Valid @RequestBody List<SensorDataDto> temperatureDataDtoList){

        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());

        List<ResponseSensorDataDto> savedTemperatureData = sensorDataService.saveSensorData(temperatureDataDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTemperatureData);
    }

    @GetMapping
    public ResponseEntity<List<ResponseSensorDataDto>> getSensorData(
            @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to){

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        List<ResponseSensorDataDto> dtos = sensorDataService.getSensorData(from, to);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping("/last")
    public ResponseEntity<LastSensorEntryDateDto> getDateOfLastSensorDataForRespondent(@RequestParam("respondentId") UUID identityUserId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        LastSensorEntryDateDto dto = sensorDataService.getDateOfLastSensorDataForRespondent(identityUserId);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

}
