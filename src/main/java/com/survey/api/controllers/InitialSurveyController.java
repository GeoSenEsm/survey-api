package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyStateDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.InitialSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/initialsurvey")
@Tag(name = "Initial survey", description = "Endpoints for managing initial survey.")
public class InitialSurveyController {

    private final InitialSurveyService initialSurveyService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public InitialSurveyController(InitialSurveyService initialSurveyService, ClaimsPrincipalService claimsPrincipalService){
        this.initialSurveyService = initialSurveyService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @PostMapping
    @Operation(
            summary = "Create initial survey.",
            description = """
                    - Create initial survey.
                    - If an initial survey already exists in database, sending a request will override the existing initial survey.
                    - Only one initial survey exists at all times.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Initial survey created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = InitialSurveyQuestionResponseDto.class))
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<InitialSurveyQuestionResponseDto>> createInitialSurvey(@Validated @RequestBody List<@Valid CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<InitialSurveyQuestionResponseDto> initialSurveyResponseDto = initialSurveyService.createInitialSurvey(createInitialSurveyQuestionDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(initialSurveyResponseDto);
    }

    @GetMapping
    @Operation(
            summary = "Fetch initial survey.",
            description = """
                    - Fetch initial survey.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Initial survey fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = InitialSurveyQuestionResponseDto.class))
                    )),
            @ApiResponse(responseCode = "404",
                    description = "Initial survey does not exist.",
                    content = @Content
                )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<InitialSurveyQuestionResponseDto>> getInitialSurvey() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        List<InitialSurveyQuestionResponseDto> initialSurveyResponseDto = initialSurveyService.getInitialSurvey();
        return ResponseEntity.status(HttpStatus.OK).body(initialSurveyResponseDto);
    }


    @GetMapping("/state")
    @Operation(
            summary = "Check initial survey state.",
            description = """
                    - Check current initial survey state.
                    - Possible states are:
                        - `not_created`
                        - `created`
                        - `published`
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Initial survey state fetched successfully.")
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<InitialSurveyStateDto> getInitialSurveyState(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        InitialSurveyStateDto response = initialSurveyService.checkInitialSurveyState();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PatchMapping("/publish")
    @Operation(
            summary = "Publish initial survey.",
            description = """
                    - Endpoint for setting initial survey state to `published`.
                    - This operation populates respondents_group table with groups created based on initial survey.
                        - [question content] - [option content]
                        - ex. "gender - male"
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Initial survey published successfully.")
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Void> publishInitialSurvey(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        initialSurveyService.publishInitialSurveyAndCreateRespondentGroups();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
