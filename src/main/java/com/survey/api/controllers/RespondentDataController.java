package com.survey.api.controllers;

import com.survey.application.dtos.RespondentDataDto;
import com.survey.application.services.RespondentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/respondents")
public class RespondentDataController {

    private final RespondentDataService respondentDataService;

    @Autowired
    public RespondentDataController(RespondentDataService respondentDataService){
            this.respondentDataService = respondentDataService;
    }

    @PostMapping
    public ResponseEntity<String> createRespondentData(@Validated @RequestBody RespondentDataDto dto) {
            return respondentDataService.createRespondent(dto);
    }
}
