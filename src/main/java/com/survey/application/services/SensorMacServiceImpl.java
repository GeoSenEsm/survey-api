package com.survey.application.services;

import com.survey.application.dtos.AssignSensorRespondentDto;
import com.survey.application.dtos.UpdatedSensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorMac;
import com.survey.domain.models.SensorType;
import com.survey.domain.models.enums.SensorTypeCodes;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorMacRepository;
import com.survey.domain.repository.SensorTypeRepository;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SensorMacServiceImpl implements SensorMacService{
    private final SensorMacRepository sensorMacRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final IdentityUserRepository identityUserRepository;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;

    @Autowired
    public SensorMacServiceImpl(SensorMacRepository sensorMacRepository,
                                SensorTypeRepository sensorTypeRepository,
                                IdentityUserRepository identityUserRepository,
                                ClaimsPrincipalService claimsPrincipalService,
                                ModelMapper modelMapper,
                                EntityManager entityManager) {
        this.sensorMacRepository = sensorMacRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.identityUserRepository = identityUserRepository;
        this.claimsPrincipalService = claimsPrincipalService;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<SensorMacDtoOut> saveSensorMacList(List<SensorMacDtoIn> dtoList) {
        SensorType defaultType = requireTypeByCode(SensorTypeCodes.XIAOMI);

        List<SensorMac> sensorMacEntityList = dtoList.stream()
                .map(dto -> {
                    UUID typeId = dto.getSensorTypeId() != null
                            ? requireType(dto.getSensorTypeId()).getId()
                            : defaultType.getId();

                    return sensorMacRepository.findBySensorId(dto.getSensorId())
                            .map(existing -> {
                                existing.setSensorMac(dto.getSensorMac().toUpperCase());
                                existing.setSensorTypeId(typeId);
                                return existing;
                            })
                            .orElseGet(() -> {
                                SensorMac created = modelMapper.map(dto, SensorMac.class);
                                created.setSensorMac(created.getSensorMac().toUpperCase());
                                created.setSensorTypeId(typeId);
                                return created;
                            });
                })
                .toList();

        List<SensorMac> savedEntities = sensorMacRepository.saveAllAndFlush(sensorMacEntityList);
        savedEntities.forEach(entityManager::refresh);

        return toDtos(savedEntities);
    }

    @Override
    public void deleteSensorMac(String sensorId) {
        SensorMac sensorMac = sensorMacRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new NoSuchElementException("Sensor with sensorId " + sensorId + " not found."));
        sensorMacRepository.delete(sensorMac);
    }

    @Override
    public void deleteAll() {
        sensorMacRepository.deleteAll();
    }

    @Override
    @Transactional
    public SensorMacDtoOut updateSensorMacBySensorId(String sensorId, UpdatedSensorMacDtoIn updatedSensorMacDtoIn) {
        SensorMac existingSensorMac = sensorMacRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new NoSuchElementException("Sensor with sensorId " + sensorId + " not found."));

        SensorType type = requireType(updatedSensorMacDtoIn.getSensorTypeId());
        existingSensorMac.setSensorMac(updatedSensorMacDtoIn.getSensorMac().toUpperCase());
        existingSensorMac.setSensorTypeId(type.getId());

        SensorMac updatedEntity = sensorMacRepository.save(existingSensorMac);

        return toDto(updatedEntity);
    }

    @Override
    @Transactional
    public SensorMacDtoOut assignRespondent(String sensorId, AssignSensorRespondentDto dto) {
        SensorMac sensor = sensorMacRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new NoSuchElementException("Sensor with sensorId " + sensorId + " not found."));

        UUID respondentId = dto.getRespondentId();
        if (respondentId == null) {
            sensor.setRespondentId(null);
            return toDto(sensorMacRepository.save(sensor));
        }

        IdentityUser respondent = identityUserRepository.findById(respondentId)
                .orElseThrow(() -> new IllegalArgumentException("Respondent with id " + respondentId + " not found."));
        if (!"Respondent".equals(respondent.getRole())) {
            throw new IllegalArgumentException("User " + respondentId + " is not a respondent.");
        }

        sensorMacRepository.findByRespondentId(respondentId)
                .filter(other -> !other.getId().equals(sensor.getId()))
                .ifPresent(other -> {
                    other.setRespondentId(null);
                    sensorMacRepository.save(other);
                });

        sensor.setRespondentId(respondentId);
        return toDto(sensorMacRepository.save(sensor));
    }

    @Override
    public List<SensorMacDtoOut> getFullSensorMacList() {
        List<SensorMac> sensorMacList = sensorMacRepository.findAllOrderBySensorId();

        return toDtos(sensorMacList).stream()
                .sorted(Comparator.comparing(sensor -> {
                    try {
                        return Integer.parseInt(sensor.getSensorId());
                    } catch (NumberFormatException e){
                        return Integer.MAX_VALUE;
                    }
                }))
                .toList();
    }

    @Override
    public SensorMacDtoOut getSensorMacBySensorId(String sensorId) {
        SensorMac sensorMac = sensorMacRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new NoSuchElementException("Sensor with sensorId " + sensorId + " not found."));

        return toDto(sensorMac);
    }

    @Override
    public Optional<SensorMacDtoOut> getAssignedToCurrentRespondent() {
        IdentityUser current = claimsPrincipalService.findIdentityUser();
        return sensorMacRepository.findByRespondentId(current.getId()).map(this::toDto);
    }

    @Override
    public List<SensorTypeDtoOut> getSensorTypes() {
        return sensorTypeRepository.findAllOrderByName().stream()
                .map(type -> modelMapper.map(type, SensorTypeDtoOut.class))
                .toList();
    }

    private SensorType requireType(UUID sensorTypeId) {
        return sensorTypeRepository.findById(sensorTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Sensor type " + sensorTypeId + " not found."));
    }

    private SensorType requireTypeByCode(String code) {
        return sensorTypeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Sensor type '" + code + "' is not seeded."));
    }

    private List<SensorMacDtoOut> toDtos(List<SensorMac> entities) {
        Set<UUID> respondentIds = entities.stream()
                .map(SensorMac::getRespondentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, IdentityUser> respondentsById = identityUserRepository.findAllById(respondentIds).stream()
                .collect(Collectors.toMap(IdentityUser::getId, Function.identity()));

        Set<UUID> typeIds = entities.stream()
                .map(SensorMac::getSensorTypeId)
                .collect(Collectors.toSet());

        Map<UUID, SensorType> typesById = sensorTypeRepository.findAllById(typeIds).stream()
                .collect(Collectors.toMap(SensorType::getId, Function.identity()));

        return entities.stream()
                .map(entity -> toDto(entity, respondentsById, typesById))
                .toList();
    }

    private SensorMacDtoOut toDto(SensorMac entity) {
        return toDtos(List.of(entity)).get(0);
    }

    private SensorMacDtoOut toDto(SensorMac entity,
                                  Map<UUID, IdentityUser> respondentsById,
                                  Map<UUID, SensorType> typesById) {
        SensorMacDtoOut dto = modelMapper.map(entity, SensorMacDtoOut.class);
        if (entity.getRespondentId() != null) {
            IdentityUser respondent = respondentsById.get(entity.getRespondentId());
            if (respondent != null) {
                dto.setRespondentUsername(respondent.getUsername());
            }
        }
        SensorType type = typesById.get(entity.getSensorTypeId());
        if (type != null) {
            dto.setSensorTypeCode(type.getCode());
            dto.setSensorTypeName(type.getName());
        }
        return dto;
    }
}
