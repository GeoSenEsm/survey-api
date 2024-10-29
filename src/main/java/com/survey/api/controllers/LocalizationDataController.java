package com.survey.api.controllers;

import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.application.services.LocalizationDataService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/localization")
public class LocalizationDataController {

    private final LocalizationDataService localizationDataService;

    public LocalizationDataController(LocalizationDataService localizationDataService) {
        this.localizationDataService = localizationDataService;
    }


    @PostMapping
    @CrossOrigin
    @SecurityRequirement(name = "bearerAuth")
    public List<ResponseLocalizationDto> saveLocalizationData(){

        return null;
    }
}
