package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.LocalizationDataService;
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
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public LocalizationDataController(LocalizationDataService localizationDataService, ClaimsPrincipalService claimsPrincipalService) {
        this.localizationDataService = localizationDataService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    @CrossOrigin
    public ResponseEntity<List<ResponseLocalizationDto>> saveLocalizationData(
            @Valid @RequestBody List<LocalizationDataDto> localizationDataDtos){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());

        List<ResponseLocalizationDto> saveLocalizationData = localizationDataService.saveLocalizationData(localizationDataDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(saveLocalizationData);
    }

    @GetMapping
    @CrossOrigin
    public ResponseEntity<List<ResponseLocalizationDto>> getLocalizationData(
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to,
            @RequestParam(value = "respondentId", required = false) UUID identityUserId,
            @RequestParam(value = "surveyId", required = false) UUID surveyId,
            @RequestParam(value = "outsideResearchArea", required = false) boolean outsideResearchArea){

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        List<ResponseLocalizationDto> dtoList = localizationDataService.getLocalizationData(from, to, identityUserId, surveyId, outsideResearchArea);
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }


}
