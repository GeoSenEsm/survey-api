package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.UpdatedSensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SensorMacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/sensormac")
@Tag(name = "Sensor MAC addresses", description = "Endpoints for managing sensor MAC addresses table.")
public class SensorMacController {
    private final ClaimsPrincipalService claimsPrincipalService;
    private final SensorMacService sensorMacService;

    @Autowired
    public SensorMacController(ClaimsPrincipalService claimsPrincipalService, SensorMacService sensorMacService) {
        this.claimsPrincipalService = claimsPrincipalService;
        this.sensorMacService = sensorMacService;
    }


    @PostMapping
    @Operation(
            summary = "Save a list of sensorId - sensorMac pairs.",
            description = """
                    - Allows admin to create a list that connects sensorId (can be a simple number written on the physical sensor) with sensor MAC address.
                    - SensorId must be unique.
                    - Performing multiple request will not delete existing data. New rows (based on sensorId) will be added.
                    - If the request will contain sensorId that already exists in database, the mac address will be updated.
                    - Sensor MAC addresses are necessary to distinguish which sensor belongs to which respondent.
                    - If many respondents are together with their sensors, the mobile application must know which sensor should it connect to. It does that by knowing sensor MAC address.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "SensorId - sensorMac table created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SensorMacDtoOut.class))
                    ))
    })
    public ResponseEntity<List<SensorMacDtoOut>> saveSensorMacList(@Valid @RequestBody List<SensorMacDtoIn> sensorMacDtoList){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        List<SensorMacDtoOut> responseDtoList = sensorMacService.saveSensorMacList(sensorMacDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtoList);
    }


    @DeleteMapping("/{sensorId}")
    @Operation(
            summary = "Delete sensorId - sensorMac pair.",
            description = """
                    - Allows admin to delete single sensorId - sensorMac pair.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "SensorId - sensorMac pair deleted successfully.")
    })
    public ResponseEntity<Void> deleteSensorMacBySensorId(@PathVariable @NotNull String sensorId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        sensorMacService.deleteSensorMac(sensorId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @DeleteMapping("/all")
    @Operation(
            summary = "Delete all sensorId - sensorMac pairs.",
            description = """
                    - Allows admin to delete all sensorId - sensorMac pairs.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "All sensorId - sensorMac pairs deleted successfully.")
    })
    public ResponseEntity<Void> deleteAllSensorMacs(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        sensorMacService.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PutMapping("/{sensorId}")
    @Operation(
            summary = "Update MAC address for given sensorId",
            description = """
                    - Allows admin to update sensorMac based on given sensorId.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "SensorMac of sensor with given SensorId updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SensorMacDtoOut.class)
                    ))
    })
    public ResponseEntity<SensorMacDtoOut> updateSensorMacBySensorId(
            @PathVariable @NotNull String sensorId,
            @Valid @RequestBody UpdatedSensorMacDtoIn updatedSensorMacDtoIn){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        SensorMacDtoOut responseDto = sensorMacService.updateSensorMacBySensorId(sensorId, updatedSensorMacDtoIn);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }


    @GetMapping("/all")
    @Operation(
            summary = "Fetch all sensorId - sensorMac pairs.",
            description = """
                    - Allows admin to fetch all sensorId - sensorMac pairs.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "All sensorId- sensorMac pairs fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SensorMacDtoOut.class))
                    ))
    })
    public ResponseEntity<List<SensorMacDtoOut>> getAll(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<SensorMacDtoOut> responseDtoList = sensorMacService.getFullSensorMacList();
        return ResponseEntity.status(HttpStatus.OK).body(responseDtoList);
    }


    @GetMapping(params = "sensorId")
    @Operation(
            summary = "Fetch single sensorId - sensorMac pair.",
            description = """
                    - Allows admin and respondent to fetch single sensorId - sensorMac pair.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Single sensorId - sensorMac pair fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SensorMacDtoOut.class)
                    ))
    })
    public ResponseEntity<SensorMacDtoOut> getBySensorId(@RequestParam String sensorId){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName(), Role.ADMIN.getRoleName());
        SensorMacDtoOut responseDto = sensorMacService.getSensorMacBySensorId(sensorId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
