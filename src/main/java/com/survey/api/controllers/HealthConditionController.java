package com.survey.api.controllers;

import com.survey.application.dtos.HealthConditionDto;
import com.survey.application.services.HealthConditionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healthconditions")
public class HealthConditionController {
    private final HealthConditionService healthConditionService;

    public HealthConditionController(HealthConditionService healthConditionService) {
        this.healthConditionService = healthConditionService;
    }

    @GetMapping
    public List<HealthConditionDto> getAllHealthConditions() {
        return healthConditionService.getAllHealthConditions();
    }

}
