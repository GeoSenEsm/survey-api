package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.GattProfileValidationDto;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorGattProfileWriteDto;
import com.survey.application.dtos.SensorProfileCapabilitiesDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.SensorDeviceSecretWriteDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SensorDeviceSecretService;
import com.survey.application.services.SensorGattProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/sensorprofiles")
public class SensorGattProfileController {
    private final SensorGattProfileService service;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final SensorDeviceSecretService deviceSecretService;

    public SensorGattProfileController(
            SensorGattProfileService service,
            ClaimsPrincipalService claimsPrincipalService,
            SensorDeviceSecretService deviceSecretService) {
        this.service = service;
        this.claimsPrincipalService = claimsPrincipalService;
        this.deviceSecretService = deviceSecretService;
    }

    @GetMapping("/capabilities")
    public ResponseEntity<SensorProfileCapabilitiesDto> capabilities() {
        ensureAdmin();
        return ResponseEntity.ok(service.capabilities());
    }

    @GetMapping("/sensortypes")
    public ResponseEntity<List<SensorTypeDtoOut>> listSensorTypes() {
        ensureAdmin();
        return ResponseEntity.ok(service.listProfileSensorTypes());
    }

    @GetMapping
    public ResponseEntity<List<SensorGattProfileDto>> listRevisions(@RequestParam UUID sensorTypeId) {
        ensureAdmin();
        return ResponseEntity.ok(service.listRevisions(sensorTypeId));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<SensorGattProfileDto> get(@PathVariable UUID profileId) {
        ensureAdmin();
        return ResponseEntity.ok(service.get(profileId));
    }

    @PostMapping("/{sensorTypeId}/drafts")
    public ResponseEntity<SensorGattProfileDto> createDraft(
            @PathVariable UUID sensorTypeId,
            @Valid @RequestBody SensorGattProfileWriteDto dto) {
        ensureAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDraft(sensorTypeId, dto));
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<SensorGattProfileDto> updateDraft(
            @PathVariable UUID profileId,
            @Valid @RequestBody SensorGattProfileWriteDto dto) {
        ensureAdmin();
        return ResponseEntity.ok(service.updateDraft(profileId, dto));
    }

    @PostMapping("/{profileId}/validate")
    public ResponseEntity<GattProfileValidationDto> validate(@PathVariable UUID profileId) {
        ensureAdmin();
        return ResponseEntity.ok(service.validate(profileId));
    }

    @PostMapping("/{profileId}/publish")
    public ResponseEntity<SensorGattProfileDto> publish(@PathVariable UUID profileId) {
        ensureAdmin();
        return ResponseEntity.ok(service.publish(profileId));
    }

    @PostMapping("/{sensorTypeId}/rollback/{revision}")
    public ResponseEntity<SensorGattProfileDto> rollback(
            @PathVariable UUID sensorTypeId,
            @PathVariable @Min(1) int revision) {
        ensureAdmin();
        return ResponseEntity.ok(service.rollback(sensorTypeId, revision));
    }

    @PostMapping("/types")
    public ResponseEntity<SensorTypeDtoOut> createSensorType(@Valid @RequestBody SensorTypeCreateDto dto) {
        ensureAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSensorType(dto));
    }

    @PutMapping("/devices/{sensorMacId}/secrets/{secretName}")
    public ResponseEntity<Void> putDeviceSecret(
            @PathVariable UUID sensorMacId,
            @PathVariable String secretName,
            @Valid @RequestBody SensorDeviceSecretWriteDto dto) {
        ensureAdmin();
        deviceSecretService.put(sensorMacId, secretName, dto.value());
        return ResponseEntity.noContent().build();
    }

    private void ensureAdmin() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
    }
}
