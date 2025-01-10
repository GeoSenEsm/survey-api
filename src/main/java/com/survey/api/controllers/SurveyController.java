package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.surveyDtos.*;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SurveyService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/surveys")
@Tag(name = "Surveys", description = "Endpoints for managing surveys.")
public class SurveyController {
    private final SurveyService surveyService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SurveyController(SurveyService surveyService, ClaimsPrincipalService claimsPrincipalService) {
        this.surveyService = surveyService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create new survey.",
            description = """
                    - Allows admin to create a new survey.
                    - For more info about how to create json check CreateSurveyDto schema documentation.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Survey created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseSurveyDto.class)
                    )
            )
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<ResponseSurveyDto> createSurvey(@RequestPart("json") @Validated CreateSurveyDto createSurveyDto, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        ResponseSurveyDto responseDto = surveyService.createSurvey(createSurveyDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(responseDto);
    }


    @GetMapping(params = "completionDate")
    @Operation(
            summary = "Fetch survey by completionDate.",
            description = """
                    - Allows admin to fetch surveys that are have time slots in given day.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Surveys fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseSurveyDto.class)
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseSurveyDto>> getSurveysByCompletionDate(
            @RequestParam("completionDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate completionDate) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseSurveyDto> surveys = surveyService.getSurveysByCompletionDate(completionDate);
        return ResponseEntity.ok(surveys);
    }


    @GetMapping(params = "surveyId")
    @Operation(
            summary = "Fetch survey by surveyId.",
            description = """
                    - Allows admin and respondents to fetch survey with given UUID.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Survey fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseSurveyDto.class)
                    ))
    })
    @CommonApiResponse403
    public ResponseEntity<ResponseSurveyDto> getSurveyById(@RequestParam("surveyId") UUID surveyId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        ResponseSurveyDto responseSurveyDto = surveyService.getSurveyById(surveyId);
        return ResponseEntity.ok(responseSurveyDto);
    }


    @GetMapping("/short")
    @Operation(
            summary = "Fetch brief information about surveys.",
            description = """
                    - Allows admin to fetch a list of surveyIds and their names.
                    - Used to display a list of existing surveys (their names) without fetching the whole survey contents.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Survey short fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseSurveyShortDto.class))
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseSurveyShortDto>> getShortSurveys(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseSurveyShortDto> shortSurveys = surveyService.getSurveysShort();
        return ResponseEntity.status(HttpStatus.OK).body(shortSurveys);
    }


    @GetMapping("/shortsummaries")
    @Operation(
            summary = "Fetch brief summaries of surveys.",
            description = """
        - Allows admin and respondents to fetch brief summaries of surveys.
        - Includes survey ID, name, and associated time slot details.
        - Respondent-specific behavior:
            - Fetches only active or upcoming surveys not yet participated by the respondent.
        - **Access:**
            - ADMIN
            - RESPONDENT
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Survey summaries fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseSurveyShortSummariesDto.class))
                    )),
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseSurveyShortSummariesDto>> getShortSurveysSummaries(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        List<ResponseSurveyShortSummariesDto> shortSummariesSurveys = surveyService.getSurveysShortSummaries();
        return ResponseEntity.status(HttpStatus.OK).body(shortSummariesSurveys);
    }


    @GetMapping("/allwithtimeslots")
    @Operation(
            summary = "Fetch survey with its time slots.",
            description = """
                    - Allows respondent to fetch all surveys with currently active or upcoming time slots when given survey will be active.
                    - Does not return time slots from the past.
                    - Does not return time slots in witch respondent already filled the survey.
                    - Used to provide mobile app offline functionality.
                    - Optional parameter `maxRowVersion` can be passed.
                        - Mobile app saves surveys with time slots locally in order to function offline.
                        - Mobile app can send `maxRowVersion` - it is supposed to be maximum rowVersion of surveys, timeSlots etc. that app has in its local database.
                        - If maxRowVersion from mobile app is lower than maxRowVersion from the server, it means that server has newer data and mobile app needs to update its local database.
                        - If the server does not contain newer data, NOT_MODIFIED status code will be returned.
                        - Thanks to that, number of unnecessary data fetches is reduced significantly.
                    - **Access:**
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Surveys with time slots fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseSurveyWithTimeSlotsDto.class))
                    )),
            @ApiResponse(responseCode = "304",
                    description = "Mobile app passed `maxRowVersion` parameter and the server does not contain newer data.",
                    content = @Content(mediaType = "null"
            ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseSurveyWithTimeSlotsDto>> getAllSurveysWithTimeSlots(@RequestParam(value = "maxRowVersion", required = false) Long maxRowVersionFromMobileApp){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
        if (maxRowVersionFromMobileApp != null && !surveyService.doesNewerDataExistsInDB(maxRowVersionFromMobileApp)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        List<ResponseSurveyWithTimeSlotsDto> responseSurveyWithTimeSlotsDtoList = surveyService.getAllSurveysWithTimeSlots();
        return ResponseEntity.status(HttpStatus.OK).body(responseSurveyWithTimeSlotsDtoList);

    }


    @PatchMapping("/publish")
    @Operation(
            summary = "Publish survey by ID.",
            description = """
                    - Allows admin to change given survey state from `created` to `published`.
                    - This operation cannot be undone.
                    - After publishing a survey:
                        - It is not possible to modify it.
                        - It will be send to respondents (if it has time slots set).
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Survey published successfully.",
                    content = @Content(mediaType = "null")
                    )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Void> publishSurvey(@RequestParam("surveyId") UUID surveyId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        surveyService.publishSurvey(surveyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping("/{surveyId}")
    @Operation(
            summary = "Delete survey by ID.",
            description = """
                    - Allows admin to hard delete a survey from the database.
                    - Published survey cannot be deleted, because it has already been sent to the respondents.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Survey deleted successfully.",
                    content = @Content(mediaType = "null")
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Void> deleteSurvey(@PathVariable UUID surveyId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        surveyService.deleteSurvey(surveyId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PutMapping(value = "/{surveyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update an existing survey.",
            description = """
                    - Allows admin to update an existing survey.
                    - Only not published survey can be updated.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Survey updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseSurveyDto.class)
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<ResponseSurveyDto> updateSurvey(@PathVariable UUID surveyId, @RequestPart("json") @Validated CreateSurveyDto createSurveyDto, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        ResponseSurveyDto responseSurveyDto = surveyService.updateSurvey(surveyId, createSurveyDto, files);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(responseSurveyDto);
    }
}
