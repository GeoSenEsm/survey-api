package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.LocalizationDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Geolocation", description = "Endpoints for saving and filtering respondents localization data.")
public class LocalizationDataController {

    private final LocalizationDataService localizationDataService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public LocalizationDataController(LocalizationDataService localizationDataService, ClaimsPrincipalService claimsPrincipalService) {
        this.localizationDataService = localizationDataService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @PostMapping
    @Operation(
            summary = "Send localization data points.",
            description = """
                    - Send a list of localization data points to be saved in database.
                    - `dateTime` is in UTC.
                    - **Important**: latitude and longitude can have maximum 6 decimal numbers.
                    - **Access:**
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Localization data points saved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseLocalizationDto.class))
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseLocalizationDto>> saveLocalizationData(
            @Valid @RequestBody List<LocalizationDataDto> localizationDataDtos){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());

        List<ResponseLocalizationDto> saveLocalizationData = localizationDataService.saveLocalizationData(localizationDataDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(saveLocalizationData);
    }


    @GetMapping
    @Operation(
            summary = "Fetch a list of localization data points.",
            description = """
                    - Fetch a list of localization data points that meed filtering criteria.
                    - `dateTime` is in UTC.
                    - All filters are optional. If no filters are set, all available data will be returned.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Localization data points filtered and returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseLocalizationDto.class))
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseLocalizationDto>> getLocalizationData(
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to,
            @RequestParam(value = "respondentId", required = false) UUID identityUserId,
            @RequestParam(value = "surveyId", required = false) UUID surveyId,
            @RequestParam(value = "outsideResearchArea", required = false) Boolean outsideResearchArea){

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        List<ResponseLocalizationDto> dtoList = localizationDataService.getLocalizationData(from, to, identityUserId, surveyId, outsideResearchArea);
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }


}
