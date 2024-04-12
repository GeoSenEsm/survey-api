package com.survey.api.controllers;

import com.survey.application.dtos.LifeSatisfactionDto;
import com.survey.application.services.LifeSatisfactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lifesatisfaction")
public class LifeSatisfactionController {
    private final LifeSatisfactionService lifeSatisfactionService;

    @Autowired
    public LifeSatisfactionController(LifeSatisfactionService lifeSatisfactionService){
        this.lifeSatisfactionService = lifeSatisfactionService;
    }

    @GetMapping
    public List<LifeSatisfactionDto> getAllLifeSatisfactionValues() {
        return lifeSatisfactionService.getAllLifeSatisfactionValues();
    }

}


