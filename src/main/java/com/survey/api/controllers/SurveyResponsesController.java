package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.SurveyResultDto;
import com.survey.application.dtos.surveyDtos.SendOfflineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SendOnlineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SurveyResponsesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidAttributeValueException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/surveyresponses")
@RequestScope
@Tag(name = "Survey responses", description = "Endpoints for sending survey responses and fetching results.")
public class SurveyResponsesController {
    private final SurveyResponsesService surveyResponsesService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SurveyResponsesController(SurveyResponsesService surveyResponsesService, ClaimsPrincipalService claimsPrincipalService){
        this.surveyResponsesService = surveyResponsesService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @PostMapping
    @Operation(
            summary = "Send answers to a survey that is currently active.",
            description = """
                    - Allows respondent to send answers to a survey that has a currently active time slot.
                    - When surveyStartDate is within time slot, but surveyFinishDate is up to 5 minutes after time slot finish - the response will be accepted.
                    - **Access:**
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Survey answers saved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SurveyParticipationDto.class)
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveyParticipationDto> saveSurveyResponseOnline(@Validated @RequestBody SendOnlineSurveyResponseDto sendOnlineSurveyResponseDto) throws InvalidAttributeValueException {
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
        SurveyParticipationDto surveyParticipationDto = surveyResponsesService.saveSurveyResponseOnline(sendOnlineSurveyResponseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyParticipationDto);
    }

    @PostMapping("/offline")
    @Operation(
            summary = "Send answers to a survey filled offline",
            description = """
                    - Allows respondent to send answers to a survey (many surveys) that they filled offline.
                    - Time slots can be from the past.
                    - When surveyStartDate is within time slot, but surveyFinishDate is up to 5 minutes after time slot finish - the response will be accepted.
                    - **IMPORTANT** this endpoint will always return 201 (CREATED) status code.
                        - It will perform silent validation and save only valid survey responses to the database.
                        - Survey responses that did not passed the validation (eg. required answer not present) will be lost forever.
                        - It is possible to determine witch survey responses have actually been saved. Look at response body.
                    - **Access:**
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Survey answers saved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SurveyParticipationDto.class))
                    )
            )
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<SurveyParticipationDto>> saveSurveyResponseOffline(@RequestBody List<SendOfflineSurveyResponseDto> sendOfflineSurveyResponseDtoList){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
        List<SurveyParticipationDto> surveyParticipationDtoList = surveyResponsesService.saveSurveyResponsesOffline(sendOfflineSurveyResponseDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyParticipationDtoList);
    }

    @GetMapping("/results")
    // TODO: not documented, because wgrzesik is modifying this endpoint.
    public ResponseEntity<List<SurveyResultDto>> getSurveyResults(
            @RequestParam(value = "surveyId", required = false) UUID surveyId,
            @RequestParam(value = "respondentId", required = false) UUID identityUserId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateTo) {

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<SurveyResultDto> results = surveyResponsesService.getSurveyResults(surveyId, identityUserId, dateFrom, dateTo);
        return ResponseEntity.status(HttpStatus.OK).body(results);
    }
}
