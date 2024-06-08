package com.survey.api.controllers;

import com.survey.application.dtos.HistogramDataDto;
import com.survey.application.services.ResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/results")
public class ResultsController {
    private final ResultsService resultsService;

    @Autowired
    public ResultsController(ResultsService resultsService) {
        this.resultsService = resultsService;
    }

    @GetMapping("/histogram")
    public ResponseEntity<List<HistogramDataDto>> getHistogramData(
            @RequestParam("surveyId") UUID surveyId,
            @RequestParam("date") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date){

        List<HistogramDataDto> histogramDataDtoList = resultsService.getHistogramData(surveyId, date);
        return ResponseEntity.ok(histogramDataDtoList);
    }
}
