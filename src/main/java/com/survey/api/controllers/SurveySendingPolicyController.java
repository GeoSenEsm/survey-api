package com.survey.api.controllers;


import com.survey.application.dtos.CreateSurveySendingPolicyDto;
import com.survey.application.dtos.SurveySendingPolicyDto;
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
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/surveysendingpolicies")
public class SurveySendingPolicyController {
    private final SurveySendingPolicyService surveySendingPolicyService;

    @Autowired
    public SurveySendingPolicyController(SurveySendingPolicyService surveySendingPolicyService){
        this.surveySendingPolicyService = surveySendingPolicyService;
    }

    @PostMapping
    public ResponseEntity<SurveySendingPolicyDto> createSurveySendingPolicy(
            @Validated @RequestBody CreateSurveySendingPolicyDto createSurveySendingPolicy) throws  InstanceAlreadyExistsException, NoSuchElementException,  IllegalArgumentException, BadRequestException, BadAttributeValueExpException, InstanceNotFoundException, InvalidAttributeValueException {

        SurveySendingPolicyDto createdSendingPolicy = surveySendingPolicyService.createSurveySendingPolicy(createSurveySendingPolicy);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSendingPolicy);
    }

}
