package com.survey.api.controllers;


import com.survey.api.security.Role;
import com.survey.application.dtos.*;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SurveySendingPolicyService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.BadAttributeValueExpException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;


@RestController
@RequestMapping("/api/surveysendingpolicies")
public class SurveySendingPolicyController {
    private final SurveySendingPolicyService surveySendingPolicyService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SurveySendingPolicyController(SurveySendingPolicyService surveySendingPolicyService, ClaimsPrincipalService claimsPrincipalService){
        this.surveySendingPolicyService = surveySendingPolicyService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    public ResponseEntity<SurveySendingPolicyDto> createSurveySendingPolicy(
            @Validated @RequestBody CreateSurveySendingPolicyDto createSurveySendingPolicy) throws  InstanceAlreadyExistsException, NoSuchElementException,  IllegalArgumentException, BadRequestException, BadAttributeValueExpException, InstanceNotFoundException, InvalidAttributeValueException {

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        SurveySendingPolicyDto createdSendingPolicy = surveySendingPolicyService.createSurveySendingPolicy(createSurveySendingPolicy);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSendingPolicy);
    }

    @GetMapping
    public ResponseEntity<List<SurveySendingPolicyDto>> getSurveySendingPolicyBySurveyId(
            @RequestParam("surveyId") UUID surveyId) {

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<SurveySendingPolicyDto> surveysSendingPolicies = surveySendingPolicyService.getSurveysSendingPolicyById(surveyId);
        return ResponseEntity.ok(surveysSendingPolicies);
    }

    @DeleteMapping
    public ResponseEntity<List<SurveySendingPolicyTimesDto>> deleteTimeSlotsByIds(
            @Validated @RequestBody TimeSlotsToDeleteDto timeSlotsToDelete
    ){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<SurveySendingPolicyTimesDto> deletedTimeSlots = surveySendingPolicyService.deleteTimeSlotsByIds(timeSlotsToDelete);
        return ResponseEntity.status(HttpStatus.OK).body(deletedTimeSlots);
    }

}
