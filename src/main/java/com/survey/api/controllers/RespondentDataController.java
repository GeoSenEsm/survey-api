package com.survey.api.controllers;

import com.survey.application.dtos.RespondentDataDto;
import com.survey.application.services.RespondentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> createRespondent(@Validated @RequestBody RespondentDataDto dto,
                                                   @RequestHeader(value="Authorization", required = false) String token) {
        if (token == null){
            return ResponseEntity.badRequest().body("Token is missing in headers.");
        }
        return respondentDataService.createRespondent(dto, token);
    }
}
