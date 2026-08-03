package com.survey.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorGattProfileWriteDto;
import com.survey.application.dtos.SensorProfileTemplateDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.models.SensorParameterSource;
import com.survey.domain.models.SensorType;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import com.survey.domain.repository.SensorParameterSourceRepository;
import com.survey.domain.repository.SensorTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class SensorProfileTemplateServiceImpl implements SensorProfileTemplateService {
    private final SensorTypeRepository sensorTypeRepository;
    private final SensorParameterDefinitionRepository parameterDefinitionRepository;
    private final SensorParameterSourceRepository parameterSourceRepository;
    private final SensorGattProfileService gattProfileService;
    private final InitialSurveyService initialSurveyService;
    private final ObjectMapper objectMapper;

    public SensorProfileTemplateServiceImpl(
            SensorTypeRepository sensorTypeRepository,
            SensorParameterDefinitionRepository parameterDefinitionRepository,
            SensorParameterSourceRepository parameterSourceRepository,
            SensorGattProfileService gattProfileService,
            InitialSurveyService initialSurveyService,
            ObjectMapper objectMapper) {
        this.sensorTypeRepository = sensorTypeRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterSourceRepository = parameterSourceRepository;
        this.gattProfileService = gattProfileService;
        this.initialSurveyService = initialSurveyService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<SensorProfileTemplateDto> listTemplates() {
        return SensorProfileTemplateCatalog.all().stream()
                .map(template -> new SensorProfileTemplateDto(
                        template.code(),
                        template.name(),
                        template.parameters().stream().map(SensorProfileTemplate.ParameterMapping::parameterCode).toList(),
                        sensorTypeRepository.findByCode(template.code()).isPresent()))
                .toList();
    }

    @Override
    public SensorTypeDtoOut install(String templateCode) {
        if (initialSurveyService.isPublished()) {
            throw new IllegalStateException(
                    "Sensor data setup is locked: the initial survey has already been published.");
        }
        SensorProfileTemplate template = SensorProfileTemplateCatalog.findByCode(templateCode)
                .orElseThrow(() -> new NoSuchElementException("Unknown sensor profile template: " + templateCode));
        if (sensorTypeRepository.findByCode(template.code()).isPresent()) {
            throw new IllegalStateException("Template '" + templateCode + "' is already installed.");
        }

        SensorTypeDtoOut sensorType = gattProfileService.createSensorType(
                new SensorTypeCreateDto(template.code(), template.name(), "profile", null));
        wireParameterSources(sensorType.getId(), template);
        publishTemplateProfile(sensorType.getId(), template);
        return sensorType;
    }

    private void wireParameterSources(UUID sensorTypeId, SensorProfileTemplate template) {
        SensorType sensorType = sensorTypeRepository.findById(sensorTypeId)
                .orElseThrow(() -> new NoSuchElementException("Sensor type was not found: " + sensorTypeId));
        for (SensorProfileTemplate.ParameterMapping mapping : template.parameters()) {
            SensorParameterDefinition definition = parameterDefinitionRepository.findByCode(mapping.parameterCode())
                    .orElseThrow(() -> new IllegalStateException(
                            "Parameter definition '" + mapping.parameterCode()
                                    + "' required by template '" + template.code() + "' is missing."));
            definition.setActive(true);
            parameterDefinitionRepository.save(definition);
            SensorParameterSource source = new SensorParameterSource();
            source.setParameterDefinition(definition);
            source.setSensorType(sensorType);
            source.setPriorityOrder(mapping.priorityOrder());
            parameterSourceRepository.save(source);
        }
    }

    private void publishTemplateProfile(UUID sensorTypeId, SensorProfileTemplate template) {
        SensorGattProfileDto draft = gattProfileService.createDraft(
                sensorTypeId, new SensorGattProfileWriteDto(parse(template.specJson()), template.minEngineVersion()));
        gattProfileService.publish(draft.id());
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Template BLE profile JSON is invalid.", exception);
        }
    }
}
