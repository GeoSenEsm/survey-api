package com.survey.api.controllers;

import com.survey.application.dtos.ResponseTemperatureDataEntryDto;
import com.survey.application.dtos.TemperatureDataEntryDto;
import com.survey.application.services.TemperatureDataService;
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
@RequestMapping("/api/temperaturedata")
public class TemperatureDataController {

    private final TemperatureDataService temperatureDataService;

    @Autowired
    public TemperatureDataController(TemperatureDataService temperatureDataService) {
        this.temperatureDataService = temperatureDataService;
    }

    @PostMapping
    @CrossOrigin
    public ResponseEntity<List<ResponseTemperatureDataEntryDto>> saveTemperatureData(
            @Valid @RequestBody List<TemperatureDataEntryDto> temperatureDataDtoList,
            @RequestHeader(value = "Authorization", required = false) String token){

        List<ResponseTemperatureDataEntryDto> savedTemperatureData = temperatureDataService.saveTemperatureData(token, temperatureDataDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTemperatureData);
    }

    @GetMapping
    @CrossOrigin
    public ResponseEntity<List<ResponseTemperatureDataEntryDto>> getTemperatureData(
            @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to){

        List<ResponseTemperatureDataEntryDto> dtos = temperatureDataService.getTemperatureData(from, to);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }


}
