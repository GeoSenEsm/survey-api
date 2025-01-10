package com.survey.api.controllers;


import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.*;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SurveySendingPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.BadAttributeValueExpException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;


@RestController
@RequestMapping("/api/surveysendingpolicies")
@Tag(name = "Sending policies", description = "Endpoints for managing survey time slots.")
public class SurveySendingPolicyController {
    private final SurveySendingPolicyService surveySendingPolicyService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SurveySendingPolicyController(SurveySendingPolicyService surveySendingPolicyService, ClaimsPrincipalService claimsPrincipalService){
        this.surveySendingPolicyService = surveySendingPolicyService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @PostMapping
    @Operation(
            summary = "Create new survey sending policy.",
            description = """
                    - Allows admin to create a new survey sending policy.
                    - Survey sending policy is a list of time slots when survey will be active for respondents.
                    - For more info check CreateSurveySendingPolicyDto schema documentation.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Survey sending policy created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SurveySendingPolicyDto.class)
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySendingPolicyDto> createSurveySendingPolicy(
            @Validated @RequestBody CreateSurveySendingPolicyDto createSurveySendingPolicy) throws  InstanceAlreadyExistsException, NoSuchElementException,  IllegalArgumentException, BadRequestException, BadAttributeValueExpException, InstanceNotFoundException, InvalidAttributeValueException {

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        SurveySendingPolicyDto createdSendingPolicy = surveySendingPolicyService.createSurveySendingPolicy(createSurveySendingPolicy);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSendingPolicy);
    }


    @GetMapping
    @Operation(
            summary = "Fetch survey sending policies by surveyId",
            description = """
                    - Allows admin to fetch a list of survey sending policies.
                    - There is a bit of unnecessary redundancy here.
                        - There could be just a list of time slots for given survey.
                        - Wrapping time slots in survey sending policies if a relict from past system requirement.
                        - For now it is left like that, because updating it would be a big code refactor.
                        - To be improved in the future.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Survey sending policies fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SurveySendingPolicyDto.class))
                    )
            )
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<SurveySendingPolicyDto>> getSurveySendingPolicyBySurveyId(
            @RequestParam("surveyId") UUID surveyId) {

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<SurveySendingPolicyDto> surveysSendingPolicies = surveySendingPolicyService.getSurveysSendingPolicyById(surveyId);
        return ResponseEntity.ok(surveysSendingPolicies);
    }

    @DeleteMapping
    @Operation(
            summary = "Delete time slots by ids.",
            description = """
                    - Allows admin to soft delete a list of time slots.
                    - Flag `is_deleted` is set to true.
                    - Soft deleted time slot won't be returned in any endpoint.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Time slots deleted successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SurveySendingPolicyTimesDto.class))
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<SurveySendingPolicyTimesDto>> deleteTimeSlotsByIds(
            @Validated @RequestBody TimeSlotsToDeleteDto timeSlotsToDelete
    ){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<SurveySendingPolicyTimesDto> deletedTimeSlots = surveySendingPolicyService.deleteTimeSlotsByIds(timeSlotsToDelete);
        return ResponseEntity.status(HttpStatus.OK).body(deletedTimeSlots);
    }

}
