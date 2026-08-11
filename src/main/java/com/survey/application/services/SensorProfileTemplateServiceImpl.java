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
import com.survey.domain.models.SensorType;
import com.survey.domain.models.SensorTypeParameter;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import com.survey.domain.repository.SensorTypeParameterRepository;
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
    private final SensorTypeParameterRepository sensorTypeParameterRepository;
    private final SensorGattProfileService gattProfileService;
    private final InitialSurveyService initialSurveyService;
    private final ObjectMapper objectMapper;

    public SensorProfileTemplateServiceImpl(
            SensorTypeRepository sensorTypeRepository,
            SensorParameterDefinitionRepository parameterDefinitionRepository,
            SensorTypeParameterRepository sensorTypeParameterRepository,
            SensorGattProfileService gattProfileService,
            InitialSurveyService initialSurveyService,
            ObjectMapper objectMapper) {
        this.sensorTypeRepository = sensorTypeRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.sensorTypeParameterRepository = sensorTypeParameterRepository;
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
        initialSurveyService.requireNotPublished();
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

    /**
     * Built-in templates reuse a small set of permanently-seeded parameter codes by convention
     * (see V30/V31/V34/V35 migrations), so installing one auto-populates and immediately promotes
     * ("uses") the new sensor type's raw catalog row for each mapped code — no manual admin
     * promotion step, unlike a custom sensor type's own raw parameters.
     */
    private void wireParameterSources(UUID sensorTypeId, SensorProfileTemplate template) {
        SensorType sensorType = sensorTypeRepository.findById(sensorTypeId)
                .orElseThrow(() -> new NoSuchElementException("Sensor type was not found: " + sensorTypeId));
        for (SensorProfileTemplate.ParameterMapping mapping : template.parameters()) {
            SensorParameterDefinition definition = parameterDefinitionRepository.findByCode(mapping.parameterCode())
                    .orElseThrow(() -> new IllegalStateException(
                            "Parameter definition '" + mapping.parameterCode()
                                    + "' required by template '" + template.code() + "' is missing."));

            SensorTypeParameter rawParameter = new SensorTypeParameter();
            rawParameter.setSensorType(sensorType);
            rawParameter.setCode(definition.getCode());
            rawParameter.setName(definition.getName());
            rawParameter.setDataType(definition.getDataType());
            rawParameter.setUnit(definition.getUnit());
            rawParameter.setUsedParameter(definition);
            rawParameter.setPriorityOrder(mapping.priorityOrder());
            sensorTypeParameterRepository.save(rawParameter);
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
