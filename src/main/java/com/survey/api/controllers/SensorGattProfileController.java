package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.GattProfileValidationDto;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorGattProfileWriteDto;
import com.survey.application.dtos.SensorProfileCapabilitiesDto;
import com.survey.application.dtos.SensorProfileTemplateDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SensorTypeParameterEditDto;
import com.survey.application.dtos.UseSensorTypeParameterDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SensorGattProfileService;
import com.survey.application.services.SensorProfileTemplateService;
import com.survey.application.services.SensorTypeParameterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final SensorProfileTemplateService templateService;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final SensorTypeParameterService sensorTypeParameterService;

    public SensorGattProfileController(
            SensorGattProfileService service,
            SensorProfileTemplateService templateService,
            ClaimsPrincipalService claimsPrincipalService,
            SensorTypeParameterService sensorTypeParameterService) {
        this.service = service;
        this.templateService = templateService;
        this.claimsPrincipalService = claimsPrincipalService;
        this.sensorTypeParameterService = sensorTypeParameterService;
    }

    @GetMapping("/templates")
    public ResponseEntity<List<SensorProfileTemplateDto>> listTemplates() {
        ensureAdmin();
        return ResponseEntity.ok(templateService.listTemplates());
    }

    @PostMapping("/templates/{templateCode}/install")
    public ResponseEntity<SensorTypeDtoOut> installTemplate(@PathVariable String templateCode) {
        ensureAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.install(templateCode));
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

    @DeleteMapping("/types/{sensorTypeId}")
    public ResponseEntity<Void> deleteSensorType(@PathVariable UUID sensorTypeId) {
        ensureAdmin();
        service.deleteSensorType(sensorTypeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types/{sensorTypeId}/parameters")
    public ResponseEntity<List<SensorTypeParameterDto>> listSensorTypeParameters(@PathVariable UUID sensorTypeId) {
        ensureAdmin();
        return ResponseEntity.ok(sensorTypeParameterService.list(sensorTypeId));
    }

    @PostMapping("/types/{sensorTypeId}/parameters")
    public ResponseEntity<SensorTypeParameterDto> createSensorTypeParameter(
            @PathVariable UUID sensorTypeId,
            @Valid @RequestBody SensorTypeParameterCreateDto dto) {
        ensureAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorTypeParameterService.create(sensorTypeId, dto));
    }

    @PutMapping("/types/{sensorTypeId}/parameters/{id}")
    public ResponseEntity<SensorTypeParameterDto> updateSensorTypeParameter(
            @PathVariable UUID sensorTypeId,
            @PathVariable UUID id,
            @Valid @RequestBody SensorTypeParameterEditDto dto) {
        ensureAdmin();
        return ResponseEntity.ok(sensorTypeParameterService.update(sensorTypeId, id, dto));
    }

    @DeleteMapping("/types/{sensorTypeId}/parameters/{id}")
    public ResponseEntity<Void> deleteSensorTypeParameter(
            @PathVariable UUID sensorTypeId,
            @PathVariable UUID id) {
        ensureAdmin();
        sensorTypeParameterService.delete(sensorTypeId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/types/{sensorTypeId}/parameters/{id}/use")
    public ResponseEntity<SensorTypeParameterDto> useSensorTypeParameter(
            @PathVariable UUID sensorTypeId,
            @PathVariable UUID id,
            @Valid @RequestBody UseSensorTypeParameterDto dto) {
        ensureAdmin();
        return ResponseEntity.ok(sensorTypeParameterService.use(sensorTypeId, id, dto));
    }

    @PostMapping("/types/{sensorTypeId}/parameters/{id}/unuse")
    public ResponseEntity<SensorTypeParameterDto> unuseSensorTypeParameter(
            @PathVariable UUID sensorTypeId,
            @PathVariable UUID id) {
        ensureAdmin();
        return ResponseEntity.ok(sensorTypeParameterService.unuse(sensorTypeId, id));
    }

    private void ensureAdmin() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
    }
}
