package com.survey.api.controllers;


import com.survey.application.dtos.CreateSurveySendingPolicyDto;
import com.survey.application.dtos.SurveySendingPolicyDto;
import com.survey.application.services.SurveySendingPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.InstanceAlreadyExistsException;
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
            @Validated @RequestBody CreateSurveySendingPolicyDto createSurveySendingPolicy) throws  InstanceAlreadyExistsException, NoSuchElementException,  IllegalArgumentException {

        SurveySendingPolicyDto createdSendingPolicy = surveySendingPolicyService.addSurveySendingPolicy(createSurveySendingPolicy);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSendingPolicy);
    }

}
