package com.survey.api.controllers;

import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;
import com.survey.application.services.SurveyResponsesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidAttributeValueException;


@RestController
@RequestMapping("/api/surveyresponses")
public class SurveyResponsesController {
    private final SurveyResponsesService surveyResponsesService;

    @Autowired
    public SurveyResponsesController(SurveyResponsesService surveyResponsesService){
        this.surveyResponsesService = surveyResponsesService;
    }
    @CrossOrigin
    @PostMapping
    public ResponseEntity<SurveyParticipationDto> saveSurveyResponse(@Validated @RequestBody SendSurveyResponseDto sendSurveyResponseDto, @RequestHeader(value="Authorization", required = false) String token) throws InvalidAttributeValueException {
        SurveyParticipationDto surveyParticipationDto = surveyResponsesService.saveSurveyResponse(sendSurveyResponseDto, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyParticipationDto);
    }


}
