package com.survey.api.controllers;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.application.services.LocalizationDataService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localization")
@Validated
public class LocalizationDataController {

    private final LocalizationDataService localizationDataService;

    @Autowired
    public LocalizationDataController(LocalizationDataService localizationDataService) {
        this.localizationDataService = localizationDataService;
    }

    @PostMapping
    @CrossOrigin
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ResponseLocalizationDto>> saveLocalizationData(
            @Valid @RequestBody List<LocalizationDataDto> localizationDataDtos){

        List<ResponseLocalizationDto> response = localizationDataService.saveLocalizationData(localizationDataDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
