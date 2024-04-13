package com.survey.api.controllers;

import com.survey.application.dtos.QualityOfSleepDto;
import com.survey.application.services.QualityOfSleepService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qualityofsleep")
public class QualityOfSleepController {
    private final QualityOfSleepService qualityOfSleepService;

    public QualityOfSleepController(QualityOfSleepService qualityOfSleepService) {
        this.qualityOfSleepService = qualityOfSleepService;
    }

    @GetMapping
    public List<QualityOfSleepDto> getAllQualityOfSleep() {
        return qualityOfSleepService.getAllQualityOfSleep();
    }

}
