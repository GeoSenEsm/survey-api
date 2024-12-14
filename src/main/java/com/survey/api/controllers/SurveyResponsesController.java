package com.survey.api.controllers;

import com.survey.application.dtos.SurveyResultDto;
import com.survey.application.dtos.surveyDtos.SendOfflineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SendOnlineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;
import com.survey.application.services.SurveyResponsesService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidAttributeValueException;
import java.time.OffsetDateTime;
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
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SurveyParticipationDto> saveSurveyResponseOnline(@Validated @RequestBody SendOnlineSurveyResponseDto sendOnlineSurveyResponseDto) throws InvalidAttributeValueException {
        SurveyParticipationDto surveyParticipationDto = surveyResponsesService.saveSurveyResponseOnline(sendOnlineSurveyResponseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyParticipationDto);
    }

    @CrossOrigin
    @PostMapping("/offline")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<SurveyParticipationDto>> saveSurveyResponseOffline(@RequestBody List<SendOfflineSurveyResponseDto> sendOfflineSurveyResponseDtoList){
        List<SurveyParticipationDto> surveyParticipationDtoList = surveyResponsesService.saveSurveyResponsesOffline(sendOfflineSurveyResponseDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyParticipationDtoList);
    }

    @GetMapping("/results")
    @CrossOrigin
    public ResponseEntity<List<SurveyResultDto>> getSurveyResults(
            @RequestParam("surveyId") UUID surveyId,
            @RequestParam("dateFrom") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateFrom,
            @RequestParam("dateTo") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateTo) {

        List<SurveyResultDto> results = surveyResponsesService.getSurveyResults(surveyId, dateFrom, dateTo);
        return ResponseEntity.status(HttpStatus.OK).body(results);
    }
}
