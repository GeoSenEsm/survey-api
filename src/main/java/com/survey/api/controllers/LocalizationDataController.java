package com.survey.api.controllers;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.application.services.LocalizationDataService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

        List<ResponseLocalizationDto> saveLocalizationData = localizationDataService.saveLocalizationData(localizationDataDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(saveLocalizationData);
    }

    @GetMapping
    @CrossOrigin
    public ResponseEntity<List<ResponseLocalizationDto>> getLocalizationData(
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to,
            @RequestParam(value = "respondentId", required = false) UUID respondentId,
            @RequestParam(value = "surveyId", required = false) UUID surveyId,
            @RequestParam(value = "outsideResearchArea", required = false) boolean outsideResearchArea){

        List<ResponseLocalizationDto> dtoList = localizationDataService.getLocalizationData(from, to, respondentId, surveyId, outsideResearchArea);
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }


}
