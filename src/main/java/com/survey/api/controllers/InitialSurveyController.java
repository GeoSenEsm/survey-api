package com.survey.api.controllers;

import com.survey.application.dtos.initialSurvey.CreateInitialSurveyDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyResponseDto;
import com.survey.application.services.InitialSurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/initialsurvey")
public class InitialSurveyController {

    private final InitialSurveyService initialSurveyService;

    @Autowired
    public InitialSurveyController(InitialSurveyService initialSurveyService){
        this.initialSurveyService = initialSurveyService;
    }
    @PostMapping
    public ResponseEntity<InitialSurveyResponseDto> createInitialSurvey(@Validated @RequestBody CreateInitialSurveyDto createInitialSurveyDto) {
        InitialSurveyResponseDto initialSurveyResponseDto = initialSurveyService.createInitialSurvey(createInitialSurveyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(initialSurveyResponseDto);
    }
    @GetMapping()
    public ResponseEntity<InitialSurveyResponseDto> getInitialSurveyById() {
        InitialSurveyResponseDto initialSurveyResponseDto = initialSurveyService.getInitialSurvey();
        return ResponseEntity.status(HttpStatus.OK).body(initialSurveyResponseDto);
    }
}
