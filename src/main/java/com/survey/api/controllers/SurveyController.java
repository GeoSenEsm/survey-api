package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.surveyDtos.*;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SurveyService;
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
public class SurveyController {
    private final SurveyService surveyService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SurveyController(SurveyService surveyService, ClaimsPrincipalService claimsPrincipalService) {
        this.surveyService = surveyService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseSurveyDto> createSurvey(@RequestPart("json") @Validated CreateSurveyDto createSurveyDto, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        ResponseSurveyDto responseDto = surveyService.createSurvey(createSurveyDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(responseDto);
    }

    @GetMapping(params = "completionDate")
    public ResponseEntity<List<ResponseSurveyDto>> getSurveysByCompletionDate(
            @RequestParam("completionDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate completionDate) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseSurveyDto> surveys = surveyService.getSurveysByCompletionDate(completionDate);
        return ResponseEntity.ok(surveys);
    }

    @GetMapping(params = "surveyId")
    public ResponseEntity<ResponseSurveyDto> getSurveyById(@RequestParam("surveyId") UUID surveyId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        ResponseSurveyDto responseSurveyDto = surveyService.getSurveyById(surveyId);
        return ResponseEntity.ok(responseSurveyDto);
    }

    @GetMapping("/short")
    public ResponseEntity<List<ResponseSurveyShortDto>> getShortSurveys(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseSurveyShortDto> shortSurveys = surveyService.getSurveysShort();
        return ResponseEntity.status(HttpStatus.OK).body(shortSurveys);
    }

    @GetMapping("/shortsummaries")
    public ResponseEntity<List<ResponseSurveyShortSummariesDto>> getShortSurveysSummaries(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        List<ResponseSurveyShortSummariesDto> shortSummariesSurveys = surveyService.getSurveysShortSummaries();
        return ResponseEntity.status(HttpStatus.OK).body(shortSummariesSurveys);
    }

    @GetMapping("/allwithtimeslots")
    public ResponseEntity<List<ResponseSurveyWithTimeSlotsDto>> getAllSurveysWithTimeSlots(@RequestParam(value = "maxRowVersion", required = false) Long maxRowVersionFromMobileApp){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
        if (maxRowVersionFromMobileApp != null && !surveyService.doesNewerDataExistsInDB(maxRowVersionFromMobileApp)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        List<ResponseSurveyWithTimeSlotsDto> responseSurveyWithTimeSlotsDtoList = surveyService.getAllSurveysWithTimeSlots();
        return ResponseEntity.status(HttpStatus.OK).body(responseSurveyWithTimeSlotsDtoList);

    }

    @PatchMapping("/publish")
    public ResponseEntity<Void> publishSurvey(@RequestParam("surveyId") UUID surveyId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        surveyService.publishSurvey(surveyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable UUID surveyId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        surveyService.deleteSurvey(surveyId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping(value = "/{surveyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseSurveyDto> updateSurvey(@PathVariable UUID surveyId, @RequestPart("json") @Validated CreateSurveyDto createSurveyDto, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        ResponseSurveyDto responseSurveyDto = surveyService.updateSurvey(surveyId, createSurveyDto, files);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(responseSurveyDto);
    }
}
