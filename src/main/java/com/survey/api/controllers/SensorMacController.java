package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.UpdatedSensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SensorMacService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/sensormac")
public class SensorMacController {
    private final ClaimsPrincipalService claimsPrincipalService;
    private final SensorMacService sensorMacService;

    @Autowired
    public SensorMacController(ClaimsPrincipalService claimsPrincipalService, SensorMacService sensorMacService) {
        this.claimsPrincipalService = claimsPrincipalService;
        this.sensorMacService = sensorMacService;
    }

    @PostMapping
    public ResponseEntity<List<SensorMacDtoOut>> saveSensorMacList(@Valid @RequestBody List<SensorMacDtoIn> sensorMacDtoList){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        List<SensorMacDtoOut> responseDtoList = sensorMacService.saveSensorMacList(sensorMacDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtoList);
    }

    @DeleteMapping("/{sensorId}")
    public ResponseEntity<Void> deleteSensorMacBySensorId(@PathVariable @NotNull String sensorId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        sensorMacService.deleteSensorMac(sensorId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{sensorId}")
    public ResponseEntity<SensorMacDtoOut> updateSensorMacBySensorId(
            @PathVariable @NotNull String sensorId,
            @Valid @RequestBody UpdatedSensorMacDtoIn updatedSensorMacDtoIn){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        SensorMacDtoOut responseDto = sensorMacService.updateSensorMacBySensorId(sensorId, updatedSensorMacDtoIn);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SensorMacDtoOut>> getAll(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<SensorMacDtoOut> responseDtoList = sensorMacService.getFullSensorMacList();
        return ResponseEntity.status(HttpStatus.OK).body(responseDtoList);
    }

    @GetMapping(params = "sensorId")
    public ResponseEntity<SensorMacDtoOut> getBySensorId(@RequestParam String sensorId){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName(), Role.ADMIN.getRoleName());
        SensorMacDtoOut responseDto = sensorMacService.getSensorMacBySensorId(sensorId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
