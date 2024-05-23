package com.survey.api.controllers;

import com.survey.application.dtos.surveyDtos.CreateSurveyRequestDto;
import com.survey.application.services.SurveyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<String> createSurvey(@Validated @RequestBody CreateSurveyRequestDto createSurveyRequestDto){
        surveyService.createSurvey(createSurveyRequestDto);
        return ResponseEntity.ok("Survey created");
    }
}
