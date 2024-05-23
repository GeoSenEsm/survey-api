package com.survey.api.controllers;

import com.survey.application.dtos.surveyDtos.CreateSurveyDto;
import com.survey.application.dtos.surveyDtos.ResponseSurveyDto;
import com.survey.application.services.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    @Autowired
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping
    public ResponseEntity<ResponseSurveyDto> createSurvey(@Validated @RequestBody CreateSurveyDto createSurveyDto){
        ResponseSurveyDto responseDto = surveyService.createSurvey(createSurveyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
