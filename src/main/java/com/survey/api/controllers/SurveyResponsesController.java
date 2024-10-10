package com.survey.api.controllers;

import com.survey.application.dtos.SurveyResultDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;
import com.survey.application.services.SurveyResponsesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidAttributeValueException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/surveyresponses")
@RequestScope
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

    @GetMapping("/results")
    @CrossOrigin
    public ResponseEntity<List<SurveyResultDto>> getSurveyResults(
            @RequestParam("surveyId") UUID surveyId,
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam("dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        List<SurveyResultDto> results = surveyResponsesService.getSurveyResults(surveyId, dateFrom, dateTo);
        return ResponseEntity.status(HttpStatus.OK).body(results);
    }
}
