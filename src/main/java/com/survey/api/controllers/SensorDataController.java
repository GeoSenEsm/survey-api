package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SensorDataService;
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

@Validated
@RestController
@RequestMapping("/api/sensordata")
@Tag(name = "Temperature sensors", description = "Endpoints for managing data from temperature sensors.")
public class SensorDataController {

    private final SensorDataService sensorDataService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SensorDataController(SensorDataService sensorDataService, ClaimsPrincipalService claimsPrincipalService) {
        this.sensorDataService = sensorDataService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @PostMapping
    @Operation(
            summary = "Save sensor readings.",
            description = """
                    - Allows respondents to send temperature and humidity measurements from their sensors.
                    - **Access:**
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "All sensor readings saved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseSensorDataDto.class))
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseSensorDataDto>> saveSensorData(
            @Valid @RequestBody List<SensorDataDto> temperatureDataDtoList){

        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());

        List<ResponseSensorDataDto> savedTemperatureData = sensorDataService.saveSensorData(temperatureDataDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTemperatureData);
    }


    @GetMapping
    @Operation(
            summary = "Fetch sensor readings.",
            description = """
                    - Allows to fetch sensor readings filtered by date and respondentId.
                    - Date and time must be passed in UTC.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sensor readings fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseSensorDataDto.class))
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseSensorDataDto>> getSensorData(
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to,
            @RequestParam(value = "respondentId", required = false) UUID identityUserId){

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        List<ResponseSensorDataDto> dtos = sensorDataService.getSensorData(from, to, identityUserId);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping("/last")
    @Operation(
            summary = "Check the date of last saved sensor reading for given respondent.",
            description = """
                    - Returns the date of the most recent sensor reading saved in the database for respondent with given ID
                    - Use case:
                        - Respondent is using mobile application in offline mode and some sensor readings are saved locally.
                        - When the device regains internet access, this endpoint can be used to determine which sensor readings should be sent to the server.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Date of last saved sensor reading fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LastSensorEntryDateDto.class)
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<LastSensorEntryDateDto> getDateOfLastSensorDataForRespondent(@RequestParam("respondentId") UUID identityUserId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        LastSensorEntryDateDto dto = sensorDataService.getDateOfLastSensorDataForRespondent(identityUserId);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

}
