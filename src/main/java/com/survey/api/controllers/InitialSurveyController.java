package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyStateDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.InitialSurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/initialsurvey")
public class InitialSurveyController {

    private final InitialSurveyService initialSurveyService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public InitialSurveyController(InitialSurveyService initialSurveyService, ClaimsPrincipalService claimsPrincipalService){
        this.initialSurveyService = initialSurveyService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    public ResponseEntity<List<InitialSurveyQuestionResponseDto>> createInitialSurvey(@Validated @RequestBody List<CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<InitialSurveyQuestionResponseDto> initialSurveyResponseDto = initialSurveyService.createInitialSurvey(createInitialSurveyQuestionDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(initialSurveyResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<InitialSurveyQuestionResponseDto>> getInitialSurvey() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        List<InitialSurveyQuestionResponseDto> initialSurveyResponseDto = initialSurveyService.getInitialSurvey();
        return ResponseEntity.status(HttpStatus.OK).body(initialSurveyResponseDto);
    }

    @GetMapping("/state")
    public ResponseEntity<InitialSurveyStateDto> getInitialSurveyState(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        InitialSurveyStateDto response = initialSurveyService.checkInitialSurveyState();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/publish")
    public ResponseEntity<Void> publishInitialSurvey(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        initialSurveyService.publishInitialSurveyAndCreateRespondentGroups();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
