package com.survey.api.controllers;

import com.survey.application.dtos.surveyDtos.*;
import com.survey.application.services.SurveyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/surveys")
@CrossOrigin
public class SurveyController {
    private final SurveyService surveyService;

    @Autowired
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseSurveyDto> createSurvey(@RequestPart("json") @Validated CreateSurveyDto createSurveyDto, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        ResponseSurveyDto responseDto = surveyService.createSurvey(createSurveyDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(responseDto);
    }

    @GetMapping(params = "completionDate")
    public ResponseEntity<List<ResponseSurveyDto>> getSurveysByCompletionDate(
            @RequestParam("completionDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate completionDate) {
        List<ResponseSurveyDto> surveys = surveyService.getSurveysByCompletionDate(completionDate);
        return ResponseEntity.ok(surveys);
    }

    @GetMapping(params = "surveyId")
    public ResponseEntity<ResponseSurveyDto> getSurveyById(@RequestParam("surveyId") UUID surveyId){
        ResponseSurveyDto responseSurveyDto = surveyService.getSurveyById(surveyId);
        return ResponseEntity.ok(responseSurveyDto);
    }

    @GetMapping("/short")
    public ResponseEntity<List<ResponseSurveyShortDto>> getShortSurveys(){
        List<ResponseSurveyShortDto> shortSurveys = surveyService.getSurveysShort();
        return ResponseEntity.status(HttpStatus.OK).body(shortSurveys);
    }

    @GetMapping("/shortsummaries")
    @CrossOrigin
    public ResponseEntity<List<ResponseSurveyShortSummariesDto>> getShortSurveysSummaries(){
        List<ResponseSurveyShortSummariesDto> shortSummariesSurveys = surveyService.getSurveysShortSummaries();
        return ResponseEntity.status(HttpStatus.OK).body(shortSummariesSurveys);
    }

    @CrossOrigin
    @GetMapping("/allwithtimeslots")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ResponseSurveyWithTimeSlotsDto>> getAllSurveysWithTimeSlots(){
        List<ResponseSurveyWithTimeSlotsDto> responseSurveyWithTimeSlotsDtoList = surveyService.getAllSurveysWithTimeSlots();
        return ResponseEntity.status(HttpStatus.OK).body(responseSurveyWithTimeSlotsDtoList);
    }

    @PatchMapping("/publish")
    public ResponseEntity<Void> publishSurvey(@RequestParam("surveyId") UUID surveyId){
        surveyService.publishSurvey(surveyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable UUID surveyId){
        surveyService.deleteSurvey(surveyId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping(value = "/{surveyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseSurveyDto> updateSurvey(@PathVariable UUID surveyId, @RequestPart("json") @Validated CreateSurveyDto createSurveyDto, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        ResponseSurveyDto responseSurveyDto = surveyService.updateSurvey(surveyId, createSurveyDto, files);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(responseSurveyDto);
    }
}
