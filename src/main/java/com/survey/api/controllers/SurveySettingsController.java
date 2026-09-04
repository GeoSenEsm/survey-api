package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.MobileSensorSetupDto;
import com.survey.application.dtos.SensorParameterDefinitionCreateDto;
import com.survey.application.dtos.SensorParameterDefinitionDto;
import com.survey.application.dtos.SensorParameterDefinitionEditDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.SurveySettingsDto;
import com.survey.application.services.ClaimsPrincipalService;
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
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/surveysettings")
@Tag(name = "Survey Settings", description = "Endpoints for study-wide survey settings.")
public class SurveySettingsController {
    private final SurveySettingsService surveySettingsService;
    private final ClaimsPrincipalService claimsPrincipalService;

    public SurveySettingsController(
            SurveySettingsService surveySettingsService,
            ClaimsPrincipalService claimsPrincipalService) {
        this.surveySettingsService = surveySettingsService;
        this.claimsPrincipalService = claimsPrincipalService;
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
                      `PUT /api/sensormac/{sensorId}/respondent` (Sensor
                      devices screen) to keep assigning physical sensors
                      to respondents after that point — always unlocked.
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
                      data type, and display order.
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

    @DeleteMapping("/sensordata/parameters/{id}")
    @Operation(
            summary = "Remove a used sensor data parameter.",
            description = """
                    - Hard-deletes one "used sensor data" parameter. There is
                      no soft-hide flag: a parameter is either on the list or
                      removed.
                    - Any raw sensor-type parameters wired to it are
                      automatically unwired (not deleted).
                    - Rejected with 409 if sensor readings have already been
                      collected for this parameter, so historical data is
                      never silently destroyed.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Parameter removed.")
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Void> deleteSensorParameterDefinition(@PathVariable UUID id) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        surveySettingsService.deleteSensorParameterDefinition(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sensordata/mobile")
    @Operation(
            summary = "Get mobile sensor setup.",
            description = """
                    - Returns global sensor data settings plus the current
                      respondent's sensor assignments (which physical
                      sensors, if any, they have — every assignment is
                      always attempted by the mobile app; assignment
                      itself is set on the Sensor devices screen, via
                      `PUT /api/sensormac/{sensorId}/respondent`).
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
