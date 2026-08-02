package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.MobileSensorSetupDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
                    - Accepts `.jpg`, `.jpeg`, or `.png` files.
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
                    - Updates global sensor data settings, parameter definitions,
                      source priorities, timeouts, and respondent assignments.
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
            @Valid @RequestBody SurveySensorDataSettingsDto dto) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(surveySettingsService.updateSensorDataSettings(dto));
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
