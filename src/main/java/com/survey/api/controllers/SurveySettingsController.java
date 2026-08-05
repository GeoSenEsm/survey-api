package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.MobileSensorSetupDto;
import com.survey.application.dtos.RespondentSensorAssignmentsUpdateDto;
import com.survey.application.dtos.ReorderSensorParameterSourcesDto;
import com.survey.application.dtos.SensorParameterDefinitionCreateDto;
import com.survey.application.dtos.SensorParameterDefinitionDto;
import com.survey.application.dtos.SensorParameterDefinitionEditDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.SurveySettingsDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SensorTypeParameterService;
import com.survey.application.services.SurveySettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/surveysettings")
@Tag(name = "Survey Settings", description = "Endpoints for study-wide survey settings.")
public class SurveySettingsController {
    private final SurveySettingsService surveySettingsService;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final SensorTypeParameterService sensorTypeParameterService;

    public SurveySettingsController(
            SurveySettingsService surveySettingsService,
            ClaimsPrincipalService claimsPrincipalService,
            SensorTypeParameterService sensorTypeParameterService) {
        this.surveySettingsService = surveySettingsService;
        this.claimsPrincipalService = claimsPrincipalService;
        this.sensorTypeParameterService = sensorTypeParameterService;
    }

    @GetMapping
    @Operation(
            summary = "Get survey settings.",
            description = """
                    - Returns study-wide survey settings (singleton).
                    - `showSendingPolicyCalendar` controls whether respondents
                      can open the sending-policy calendar in the mobile app.
                    - `csvColumnSeparator` / `csvDecimalSeparator` control CSV
                      import/export formatting in the admin panel.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Survey settings retrieved.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveySettingsDto.class)))
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySettingsDto> getSettings() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        return ResponseEntity.ok(surveySettingsService.getSettings());
    }

    @PutMapping
    @Operation(
            summary = "Update survey settings.",
            description = """
                    - Updates study-wide survey settings.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Survey settings updated.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveySettingsDto.class)))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySettingsDto> updateSettings(@Valid @RequestBody SurveySettingsDto dto) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.updateSettings(dto));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload the study logo.",
            description = """
                    - Uploads (or replaces) the study-wide logo shown to
                      respondents in the mobile app.
                    - Accepts `.jpg`, `.jpeg`, or `.png` files up to 1 MB.
                    - Rejects non-decodable image bytes and source rasters
                      larger than 8192px on either side.
                    - The image is downscaled server-side (longest side capped
                      at 512px) so the mobile app never has to fetch or render
                      a needlessly large file.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logo uploaded.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveySettingsDto.class)))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySettingsDto> uploadLogo(
            @RequestPart("file") MultipartFile file) throws IOException {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.uploadLogo(file));
    }

    @DeleteMapping("/logo")
    @Operation(
            summary = "Remove the study logo.",
            description = """
                    - Removes the study-wide logo, if one is set.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logo removed.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveySettingsDto.class)))
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySettingsDto> deleteLogo() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.deleteLogo());
    }

    @GetMapping("/sensordata")
    @Operation(
            summary = "Get sensor data settings.",
            description = """
                    - Returns global sensor data settings used by the admin panel.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sensor data settings retrieved.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveySensorDataSettingsDto.class)))
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySensorDataSettingsDto> getSensorDataSettings() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.getSensorDataSettings());
    }

    @PutMapping("/sensordata")
    @Operation(
            summary = "Update sensor data settings.",
            description = """
                    - Updates the sensor data mode and sensor type catalog.
                    - Parameter definitions ("used sensor data") are managed
                      one at a time via `POST`/`PUT /api/surveysettings/sensordata/parameters[/{id}]`,
                      not as part of this bulk replace.
                    - Rejected with 400 once the initial survey has been
                      published: at that point the study is live and the
                      shape of sensor data can no longer change. Use
                      `PUT /api/surveysettings/sensordata/assignments` to
                      keep assigning physical sensors to respondents after
                      that point.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sensor data settings updated.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveySensorDataSettingsDto.class)))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySensorDataSettingsDto> updateSensorDataSettings(
            @Valid @RequestBody SurveySensorDataSettingsWriteDto dto) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.updateSensorDataSettings(dto));
    }

    @PostMapping("/sensordata/parameters")
    @Operation(
            summary = "Create a used sensor data parameter directly.",
            description = """
                    - Creates a "used sensor data" parameter without going
                      through a sensor type's raw parameter catalog. This is
                      how manual-only parameters (no physical sensor behind
                      them) get created.
                    - To wire a parameter to a physical sensor type instead,
                      declare it in that sensor type's raw catalog
                      (`POST /api/sensorprofiles/types/{sensorTypeId}/parameters`)
                      and promote it
                      (`POST /api/sensorprofiles/types/{sensorTypeId}/parameters/{id}/use`).
                    - Rejected with 400 if the code already exists, or if the
                      (name, unit) pair is already used by another parameter.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parameter created.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SensorParameterDefinitionDto.class)))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SensorParameterDefinitionDto> createSensorParameterDefinition(
            @Valid @RequestBody SensorParameterDefinitionCreateDto dto) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveySettingsService.createSensorParameterDefinition(dto));
    }

    @PutMapping("/sensordata/parameters/{id}")
    @Operation(
            summary = "Edit a used sensor data parameter.",
            description = """
                    - Edits one "used sensor data" parameter's name, unit,
                      data type, required/active flags, and display order.
                    - `code` cannot be changed here: it is the wire-format
                      identity referenced by stored sensor readings, GATT
                      profile specs, and the mobile app.
                    - Rejected with 400 if the (name, unit) pair is already
                      used by another parameter.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameter updated.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SensorParameterDefinitionDto.class)))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SensorParameterDefinitionDto> updateSensorParameterDefinition(
            @PathVariable UUID id,
            @Valid @RequestBody SensorParameterDefinitionEditDto dto) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.updateSensorParameterDefinition(id, dto));
    }

    @PutMapping("/sensordata/parameters/{id}/sources")
    @Operation(
            summary = "Reorder a used sensor data parameter's raw sources.",
            description = """
                    - Sets the fallback priority order of every raw source
                      wired to this used parameter, in one call.
                    - `sourceIds` must contain exactly the source ids
                      currently wired to this parameter (from
                      `GET /api/surveysettings/sensordata`'s
                      `parameters[].sources[].id`), reordered as desired.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sources reordered.")
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<SensorTypeParameterDto>> reorderParameterSources(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderSensorParameterSourcesDto dto) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(sensorTypeParameterService.reorderSources(id, dto.sourceIds()));
    }

    @PutMapping("/sensordata/assignments")
    @Operation(
            summary = "Update respondent sensor assignments.",
            description = """
                    - Replaces which physical sensor (or sensor type, for
                      profile-less integrations) each respondent has
                      assigned, and their priority order.
                    - Always available, even after the initial survey is
                      published: unlike the mode, sensor type catalog, and
                      parameter definitions, assignments are expected to
                      keep changing throughout a live study as respondents
                      join or devices get swapped.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments updated.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveySensorDataSettingsDto.class)))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<SurveySensorDataSettingsDto> updateAssignments(
            @Valid @RequestBody RespondentSensorAssignmentsUpdateDto dto) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.updateAssignments(dto.assignments()));
    }

    @GetMapping("/sensordata/mobile")
    @Operation(
            summary = "Get mobile sensor setup.",
            description = """
                    - Returns global sensor data settings plus the current
                      respondent's ordered sensor assignments.
                    - **Access:**
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mobile sensor setup retrieved.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MobileSensorSetupDto.class)))
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<MobileSensorSetupDto> getMobileSensorSetup() {
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
        return ResponseEntity.ok(surveySettingsService.getMobileSensorSetup());
    }
}
