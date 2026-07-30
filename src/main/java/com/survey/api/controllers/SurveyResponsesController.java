package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.PagedResponseDto;
import com.survey.application.dtos.SurveyResultDto;
import com.survey.application.dtos.AllResultsDto;
import com.survey.application.dtos.surveyDtos.SendOfflineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SendOnlineSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SurveyResponseDocumentService;
import com.survey.application.services.SurveyResponsesService;
import com.survey.infrastructure.mongo.documents.SurveyResponseDocument;
import org.springframework.http.HttpHeaders;
import java.util.NoSuchElementException;
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
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
    private final SurveyResponseDocumentService surveyResponseDocumentService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SurveyResponsesController(SurveyResponsesService surveyResponsesService,
                                     SurveyResponseDocumentService surveyResponseDocumentService,
                                     ClaimsPrincipalService claimsPrincipalService){
        this.surveyResponsesService = surveyResponsesService;
        this.surveyResponseDocumentService = surveyResponseDocumentService;
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
    @Operation(
            summary = "Fetch survey results with optional filters.",
            description = """
                - Allows an administrator to fetch survey results based on optional filters.
                - **Filters available:**
                    - `surveyId`: Filter results by a specific survey ID.
                    - `respondentId`: Filter results by a specific respondent's ID.
                    - `dateFrom` and `dateTo`: Specify a date range for the results.
                    - `outsideResearchArea`: Specify if you want to get answers only from research area, or outside research area.
                - Uses true streaming to handle large datasets efficiently.
                - Prevents client timeout by sending data progressively.
                - **Access:**
                  - ADMIN
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Survey results retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SurveyResultDto.class))
                    )
            )
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<StreamingResponseBody> getSurveyResults(
            @RequestParam(value = "surveyId", required = false) UUID surveyId,
            @RequestParam(value = "respondentId", required = false) UUID identityUserId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateTo,
            @RequestParam(value = "outsideResearchArea", required = false) Boolean outsideResearchArea) {

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        StreamingResponseBody stream = outputStream -> {
            try {
                surveyResponsesService.streamSurveyResults(outputStream, surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea);
            } catch (Exception e) {
                throw new RuntimeException("Error streaming survey results", e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }

    @GetMapping("/results/all")
    @Operation(
            summary = "Fetch all results.",
            description = """
                - Allows an administrator to fetch all survey results, localization data and sensor data for all respondents.
                - Uses true streaming to handle large datasets efficiently.
                - Prevents client timeout by sending data progressively.
                - **Access:**
                  - ADMIN
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Results retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AllResultsDto.class))
                    )
            )
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<StreamingResponseBody> getAllSurveyResults() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        StreamingResponseBody stream = outputStream -> {
            try {
                surveyResponsesService.streamAllSurveyResults(outputStream);
            } catch (Exception e) {
                throw new RuntimeException("Error streaming all survey results", e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }

    @GetMapping("/documents")
    @Operation(
            summary = "List full response documents stored in MongoDB.",
            description = """
                - Every survey response is mirrored to MongoDB as a denormalized document
                  after the SQL transaction commits.
                - Returns a paginated list of documents, newest first.
                - **Filters (all optional):**
                    - `surveyId`, `respondentId`
                    - `dateFrom`, `dateTo` (participation date range, UTC)
                - **Access:** ADMIN
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Response documents retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagedResponseDto.class)
                    )
            )
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<PagedResponseDto<SurveyResponseDocument>> listResponseDocuments(
            @RequestParam(value = "surveyId", required = false) UUID surveyId,
            @RequestParam(value = "respondentId", required = false) UUID respondentId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateTo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        int cappedSize = Math.min(Math.max(size, 1), 200);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(surveyResponseDocumentService.find(
                surveyId, respondentId, dateFrom, dateTo, safePage, cappedSize));
    }

    @GetMapping("/documents/{participationId}/download")
    @Operation(
            summary = "Download a single response document as JSON.",
            description = "Returns the raw MongoDB document with a Content-Disposition attachment header. **Access:** ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Document not found.")
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveyResponseDocument> downloadResponseDocument(
            @PathVariable("participationId") UUID participationId) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        SurveyResponseDocument document = surveyResponseDocumentService.findById(participationId)
                .orElseThrow(() -> new NoSuchElementException("Response document not found"));

        String filename = "survey-response-" + participationId + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(document);
    }

    @GetMapping("/documents/export")
    @Operation(
            summary = "Export all filtered response documents as a ZIP archive.",
            description = """
                - Streams a ZIP archive containing one JSON entry per matching document.
                - Filters mirror `/documents` (all optional): `surveyId`, `respondentId`,
                  `dateFrom`, `dateTo`.
                - **Access:** ADMIN
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ZIP archive streamed successfully.")
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<StreamingResponseBody> exportResponseDocuments(
            @RequestParam(value = "surveyId", required = false) UUID surveyId,
            @RequestParam(value = "respondentId", required = false) UUID respondentId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime dateTo) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        String filename = "survey-responses-"
                + OffsetDateTime.now(java.time.ZoneOffset.UTC)
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss'Z'"))
                + ".zip";

        StreamingResponseBody stream = out -> surveyResponseDocumentService
                .exportZip(surveyId, respondentId, dateFrom, dateTo, out);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(stream);
    }
}
