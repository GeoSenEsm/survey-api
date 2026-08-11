package com.survey.application.services;

import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SensorTypeParameterEditDto;
import com.survey.application.dtos.UseSensorTypeParameterDto;
import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.models.SensorType;
import com.survey.domain.models.SensorTypeParameter;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import com.survey.domain.repository.SensorTypeParameterRepository;
import com.survey.domain.repository.SensorTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class SensorTypeParameterServiceImpl implements SensorTypeParameterService {
    private final SensorTypeParameterRepository sensorTypeParameterRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    private final SensorParameterDefinitionValidator parameterDefinitionValidator;
    private final InitialSurveyService initialSurveyService;

    public SensorTypeParameterServiceImpl(
            SensorTypeParameterRepository sensorTypeParameterRepository,
            SensorTypeRepository sensorTypeRepository,
            SensorParameterDefinitionRepository sensorParameterDefinitionRepository,
            SensorParameterDefinitionValidator parameterDefinitionValidator,
            InitialSurveyService initialSurveyService) {
        this.sensorTypeParameterRepository = sensorTypeParameterRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.sensorParameterDefinitionRepository = sensorParameterDefinitionRepository;
        this.parameterDefinitionValidator = parameterDefinitionValidator;
        this.initialSurveyService = initialSurveyService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorTypeParameterDto> list(UUID sensorTypeId) {
        requireSensorType(sensorTypeId);
        return sensorTypeParameterRepository.findBySensorTypeIdOrderByCode(sensorTypeId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public SensorTypeParameterDto create(UUID sensorTypeId, SensorTypeParameterCreateDto dto) {
        initialSurveyService.requireNotPublished();
        SensorType sensorType = requireSensorType(sensorTypeId);
        if (sensorTypeParameterRepository.existsBySensorTypeIdAndCode(sensorTypeId, dto.code())) {
            throw new IllegalArgumentException(
                    "Sensor type '" + sensorType.getCode() + "' already declares a raw parameter with code '"
                            + dto.code() + "'.");
        }
        SensorTypeParameter parameter = new SensorTypeParameter();
        parameter.setSensorType(sensorType);
        parameter.setCode(dto.code());
        parameter.setName(dto.name());
        parameter.setDataType(dto.dataType());
        parameter.setUnit(dto.unit());
        parameter.setPriorityOrder(0);
        return toDto(sensorTypeParameterRepository.save(parameter));
    }

    @Override
    public SensorTypeParameterDto update(UUID sensorTypeId, UUID id, SensorTypeParameterEditDto dto) {
        initialSurveyService.requireNotPublished();
        SensorTypeParameter parameter = requireOwnedParameter(sensorTypeId, id);
        parameter.setName(dto.name());
        parameter.setDataType(dto.dataType());
        parameter.setUnit(dto.unit());
        return toDto(sensorTypeParameterRepository.save(parameter));
    }

    @Override
    public void delete(UUID sensorTypeId, UUID id) {
        initialSurveyService.requireNotPublished();
        SensorTypeParameter parameter = requireOwnedParameter(sensorTypeId, id);
        if (parameter.getUsedParameter() != null) {
            throw new IllegalStateException(
                    "This raw parameter is used in the study; unuse it before deleting.");
        }
        sensorTypeParameterRepository.delete(parameter);
    }

    @Override
    public SensorTypeParameterDto use(UUID sensorTypeId, UUID id, UseSensorTypeParameterDto dto) {
        initialSurveyService.requireNotPublished();
        SensorTypeParameter parameter = requireOwnedParameter(sensorTypeId, id);

        SensorParameterDefinition usedParameter = dto.usedParameterId() != null
                ? requireUsedParameter(dto.usedParameterId())
                : createUsedParameterFrom(parameter, dto);

        parameter.setUsedParameter(usedParameter);
        parameter.setPriorityOrder(nextPriorityOrder(usedParameter.getId()));
        return toDto(sensorTypeParameterRepository.save(parameter));
    }

    @Override
    public SensorTypeParameterDto unuse(UUID sensorTypeId, UUID id) {
        initialSurveyService.requireNotPublished();
        SensorTypeParameter parameter = requireOwnedParameter(sensorTypeId, id);
        SensorParameterDefinition previouslyUsed = parameter.getUsedParameter();
        parameter.setUsedParameter(null);
        parameter.setPriorityOrder(0);
        SensorTypeParameterDto dto = toDto(sensorTypeParameterRepository.save(parameter));
        deleteIfNowSourceless(previouslyUsed);
        return dto;
    }

    /**
     * The "used sensor data" list has no soft-hide flag: a used parameter that loses its last
     * source (whichever path removed it — a single manual unuse, or every source of a disabled
     * sensor type going at once) is deleted outright rather than left dangling with nothing left
     * to collect it.
     */
    private void deleteIfNowSourceless(SensorParameterDefinition usedParameter) {
        if (usedParameter != null
                && sensorTypeParameterRepository.countByUsedParameterId(usedParameter.getId()) == 0) {
            sensorParameterDefinitionRepository.deleteById(usedParameter.getId());
        }
    }

    private SensorParameterDefinition createUsedParameterFrom(SensorTypeParameter raw, UseSensorTypeParameterDto dto) {
        if (dto.name() == null || dto.name().isBlank() || dto.dataType() == null || dto.dataType().isBlank()) {
            throw new IllegalArgumentException(
                    "name and dataType are required to create a new used parameter (or set usedParameterId to link an existing one).");
        }
        parameterDefinitionValidator.assertNameUnitAvailable(dto.name(), dto.unit(), null);
        SensorParameterDefinition definition = new SensorParameterDefinition();
        definition.setCode(raw.getCode());
        definition.setName(dto.name());
        definition.setDataType(dto.dataType());
        definition.setUnit(dto.unit());
        definition.setRequired(dto.required());
        definition.setDisplayOrder((int) sensorParameterDefinitionRepository.count());
        return sensorParameterDefinitionRepository.save(definition);
    }

    private int nextPriorityOrder(UUID usedParameterId) {
        List<SensorTypeParameter> existingSources =
                sensorTypeParameterRepository.findByUsedParameterIdOrderByPriorityOrder(usedParameterId);
        return existingSources.isEmpty()
                ? 0
                : existingSources.get(existingSources.size() - 1).getPriorityOrder() + 1;
    }

    private SensorTypeParameter requireOwnedParameter(UUID sensorTypeId, UUID id) {
        SensorTypeParameter parameter = sensorTypeParameterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sensor type parameter was not found: " + id));
        if (!parameter.getSensorType().getId().equals(sensorTypeId)) {
            throw new NoSuchElementException("Sensor type parameter was not found: " + id);
        }
        return parameter;
    }

    private SensorType requireSensorType(UUID sensorTypeId) {
        return sensorTypeRepository.findById(sensorTypeId)
                .orElseThrow(() -> new NoSuchElementException("Sensor type was not found: " + sensorTypeId));
    }

    private SensorParameterDefinition requireUsedParameter(UUID usedParameterId) {
        return sensorParameterDefinitionRepository.findById(usedParameterId)
                .orElseThrow(() -> new NoSuchElementException("Used sensor parameter was not found: " + usedParameterId));
    }

    @Override
    public List<SensorTypeParameterDto> reorderSources(UUID usedParameterId, List<UUID> orderedSourceIds) {
        initialSurveyService.requireNotPublished();
        requireUsedParameter(usedParameterId);
        List<SensorTypeParameter> current =
                sensorTypeParameterRepository.findByUsedParameterIdOrderByPriorityOrder(usedParameterId);
        Set<UUID> currentIds = current.stream().map(SensorTypeParameter::getId).collect(Collectors.toSet());
        if (current.size() != orderedSourceIds.size() || !currentIds.equals(new HashSet<>(orderedSourceIds))) {
            throw new IllegalArgumentException(
                    "orderedSourceIds must contain exactly the sources currently wired to this parameter.");
        }
        Map<UUID, SensorTypeParameter> byId = current.stream()
                .collect(Collectors.toMap(SensorTypeParameter::getId, Function.identity()));
        for (int i = 0; i < orderedSourceIds.size(); i++) {
            byId.get(orderedSourceIds.get(i)).setPriorityOrder(i);
        }
        return sensorTypeParameterRepository.saveAll(current).stream()
                .sorted(Comparator.comparingInt(SensorTypeParameter::getPriorityOrder))
                .map(this::toDto)
                .toList();
    }

    private SensorTypeParameterDto toDto(SensorTypeParameter parameter) {
        SensorParameterDefinition used = parameter.getUsedParameter();
        return new SensorTypeParameterDto(
                parameter.getId(),
                parameter.getSensorType().getId(),
                parameter.getSensorType().getCode(),
                parameter.getCode(),
                parameter.getName(),
                parameter.getDataType(),
                parameter.getUnit(),
                used != null ? used.getId() : null,
                used != null ? used.getCode() : null,
                parameter.getPriorityOrder());
    }
}
