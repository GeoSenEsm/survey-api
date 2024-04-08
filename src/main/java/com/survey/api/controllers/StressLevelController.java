package com.survey.api.controllers;

import com.survey.application.dtos.StressLevelDto;
import com.survey.application.services.StressLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/stresslevels")
public class StressLevelController {
    private final StressLevelService stressLevelService;

    @Autowired
    public StressLevelController(StressLevelService stressLevelService){
        this.stressLevelService = stressLevelService;
    }

    @GetMapping
    public List<StressLevelDto> getAllStressLevels(){
        return stressLevelService.getAllStressLevels();
    }
}
