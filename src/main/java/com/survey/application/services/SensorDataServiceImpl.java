package com.survey.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.application.dtos.SensorDataValueDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorData;
import com.survey.domain.models.SensorDataParameterValue;
import com.survey.domain.models.SensorParameterDefinition;
import com.survey.domain.models.SensorType;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorDataRepository;
import com.survey.domain.repository.SensorParameterDefinitionRepository;
import com.survey.domain.repository.SensorTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SensorDataServiceImpl implements SensorDataService {
    private final ClaimsPrincipalService claimsPrincipalService;
    private final ObjectMapper objectMapper;
    private final SensorDataRepository sensorDataRepository;
    private final IdentityUserRepository identityUserRepository;
    private final SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final EntityManager entityManager;

    @Autowired
    public SensorDataServiceImpl(
            ClaimsPrincipalService claimsPrincipalService,
            ObjectMapper objectMapper,
            SensorDataRepository sensorDataRepository,
            IdentityUserRepository identityUserRepository,
            SensorParameterDefinitionRepository sensorParameterDefinitionRepository,
            SensorTypeRepository sensorTypeRepository,
            EntityManager entityManager) {
        this.claimsPrincipalService = claimsPrincipalService;
        this.objectMapper = objectMapper;
        this.sensorDataRepository = sensorDataRepository;
        this.identityUserRepository = identityUserRepository;
        this.sensorParameterDefinitionRepository = sensorParameterDefinitionRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<ResponseSensorDataDto> saveSensorData(List<SensorDataDto> sensorDataDtoList) {
        if (sensorDataDtoList == null || sensorDataDtoList.isEmpty()){
            throw new IllegalArgumentException("Sensor data list cannot be empty.");
        }

        IdentityUser identityUser = claimsPrincipalService.findIdentityUser();

        Set<String> sourceCodes = sensorDataDtoList.stream()
                .map(SensorDataDto::getSource)
                .collect(Collectors.toSet());
        Map<String, SensorType> sensorTypesByCode = sensorTypeRepository.findAllByCodeIn(sourceCodes).stream()
                .collect(Collectors.toMap(SensorType::getCode, sensorType -> sensorType));
        Map<String, SensorParameterDefinition> parametersByCode = sensorParameterDefinitionRepository.findAll().stream()
                .collect(Collectors.toMap(SensorParameterDefinition::getCode, parameter -> parameter));

        List<SensorData> entityList = sensorDataDtoList.stream()
                        .map(dto -> toEntity(dto, identityUser, sensorTypesByCode, parametersByCode))
                        .toList();
        List<SensorData> dbEntityList = sensorDataRepository.saveAll(entityList);

        return mapToResponseDtoList(dbEntityList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseSensorDataDto> getSensorData(OffsetDateTime dateFrom, OffsetDateTime dateTo, UUID identityUserId) {
        // Internal pagination to handle large datasets
        // Fetches data in batches to prevent memory issues and timeouts. Batch size must stay
        // under SQL Server's 2100-parameter-per-query limit, since findByIdInWithFetch binds one
        // parameter per id in the batch.
        int batchSize = 2000;
        int offset = 0;
        List<ResponseSensorDataDto> allResults = new ArrayList<>();

        while (true) {
            List<ResponseSensorDataDto> batch = getSensorDataBatch(dateFrom, dateTo, identityUserId, offset, batchSize);

            if (batch.isEmpty()) {
                break; // No more data
            }

            allResults.addAll(batch);

            if (batch.size() < batchSize) {
                break; // Last batch (partial)
            }

            offset += batchSize;

            // Safety limit to prevent runaway queries
            if (offset > 100000) {
                break;
            }
        }

        return allResults;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseSensorDataDto> getSensorDataBatch(OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                                           UUID identityUserId, int offset, int limit) {
        // Pagination cannot be applied to a query that fetch-joins the "values" collection: Hibernate
        // would load the whole matching result set into memory and slice it in Java. Instead, first
        // page over just the parent ids (no collection fetch), then fetch those rows' collections in a
        // second, id-scoped query.
        List<UUID> pageIds = getPageOfSensorDataIds(dateFrom, dateTo, identityUserId, offset, limit);
        if (pageIds.isEmpty()) {
            return List.of();
        }

        List<SensorData> sensorDataList = sensorDataRepository.findByIdInWithFetch(pageIds);
        Map<UUID, SensorData> sensorDataById = sensorDataList.stream()
                .collect(Collectors.toMap(SensorData::getId, sensorData -> sensorData, (a, b) -> a, LinkedHashMap::new));

        List<SensorData> orderedPage = pageIds.stream()
                .map(sensorDataById::get)
                .filter(Objects::nonNull)
                .toList();

        return mapToResponseDtoList(orderedPage);
    }

    private List<UUID> getPageOfSensorDataIds(OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                               UUID identityUserId, int offset, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UUID> cq = cb.createQuery(UUID.class);
        Root<SensorData> root = cq.from(SensorData.class);

        List<Predicate> predicates = buildPredicates(cb, root, dateFrom, dateTo, identityUserId);

        cq.select(root.get("id"))
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .orderBy(cb.asc(root.get("dateTime")), cb.asc(root.get("id")));

        TypedQuery<UUID> query = entityManager.createQuery(cq);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        query.setHint("org.hibernate.readOnly", true);
        query.setHint("jakarta.persistence.query.timeout", 120000); // 2 minutes per batch

        return query.getResultList();
    }


    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<SensorData> root,
                                           OffsetDateTime dateFrom, OffsetDateTime dateTo, UUID identityUserId) {
        List<Predicate> predicates = new ArrayList<>();

        if (dateFrom != null && dateTo != null) {
            if (dateFrom.isAfter(dateTo)){
                throw new IllegalArgumentException("The 'from' date must be before 'to' date.");
            }
            predicates.add(cb.between(root.get("dateTime"), dateFrom, dateTo));
        } else if (dateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dateTime"), dateFrom));
        } else if (dateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dateTime"), dateTo));
        }

        if (identityUserId != null) {
            predicates.add(cb.equal(root.get("respondent").get("id"), identityUserId));
        }

        return predicates;
    }

    @Override
    public LastSensorEntryDateDto getDateOfLastSensorDataForRespondent(UUID identityUserId) {
        if (!identityUserRepository.existsById(identityUserId)){
            throw new IllegalArgumentException("Invalid respondent ID - respondent doesn't exist");
        }

        OffsetDateTime lastSensorData = sensorDataRepository
                .findDateOfLastEntryForRespondent(identityUserId)
                .orElseThrow(() -> new NoSuchElementException("No sensor data available for the specified respondent"));

        return new LastSensorEntryDateDto(lastSensorData);
    }

    @Override
    @Transactional(readOnly = true)
    public void streamSensorData(OutputStream outputStream, OffsetDateTime from, OffsetDateTime to, UUID identityUserId) throws Exception {
        // Start JSON array
        outputStream.write("[".getBytes());
        outputStream.flush();

        // TRUE STREAMING: Fetch and write in batches, not all at once. Batch size must stay under
        // SQL Server's 2100-parameter-per-query limit, since findByIdInWithFetch binds one
        // parameter per id in the batch.
        int batchSize = 2000;
        int offset = 0;
        boolean first = true;

        while (true) {
            // Fetch one batch at a time
            List<ResponseSensorDataDto> batch = getSensorDataBatch(from, to, identityUserId, offset, batchSize);

            if (batch.isEmpty()) {
                break; // No more data
            }

            // Write this batch to stream immediately
            for (ResponseSensorDataDto dto : batch) {
                if (!first) {
                    outputStream.write(",".getBytes());
                }
                first = false;

                String json = objectMapper.writeValueAsString(dto);
                outputStream.write(json.getBytes());
            }

            // Flush after each batch to keep connection alive
            outputStream.flush();

            if (batch.size() < batchSize) {
                break; // Last batch
            }

            offset += batchSize;

            // Safety limit
            if (offset > 100000) {
                break;
            }
        }

        // Close JSON array
        outputStream.write("]".getBytes());
        outputStream.flush();
    }

    private List<ResponseSensorDataDto> mapToResponseDtoList (List<SensorData> entityList){
        return entityList.stream()
                .map(entity -> {
                    return toResponseDto(entity);
                }).toList();
    }

    public SensorData toEntity(SensorDataDto dto, IdentityUser identityUser,
                                Map<String, SensorType> sensorTypesByCode,
                                Map<String, SensorParameterDefinition> parametersByCode) {
        SensorType sourceSensorType = sensorTypesByCode.get(dto.getSource());
        if (sourceSensorType == null) {
            throw new IllegalArgumentException("Unknown sensor source: " + dto.getSource());
        }

        SensorData entity = new SensorData();
        entity.setDateTime(dto.getDateTime());
        entity.setSource(dto.getSource());
        entity.setSourceSensorType(sourceSensorType);
        entity.setRespondent(identityUser);
        dto.getValues().forEach(valueDto -> {
            SensorParameterDefinition parameterDefinition = parametersByCode.get(valueDto.getParameterCode());
            if (parameterDefinition == null) {
                throw new IllegalArgumentException("Unknown sensor parameter: " + valueDto.getParameterCode());
            }
            SensorDataParameterValue value = new SensorDataParameterValue();
            value.setSensorData(entity);
            value.setParameterDefinition(parameterDefinition);
            value.setValue(valueDto.getValue());
            entity.getValues().add(value);
        });
        return entity;
    }

    public ResponseSensorDataDto toResponseDto(SensorData entity) {
        ResponseSensorDataDto responseDto = new ResponseSensorDataDto();
        responseDto.setId(entity.getId());
        responseDto.setRespondentId(entity.getRespondent().getId());
        responseDto.setDateTime(entity.getDateTime());
        responseDto.setSource(entity.getSource());
        responseDto.setValues(entity.getValues().stream()
                .map(value -> new SensorDataValueDto(
                        value.getParameterDefinition().getCode(),
                        value.getValue()))
                .toList());
        if (entity.getSurveyParticipation() != null) {
            responseDto.setSurveyId(entity.getSurveyParticipation().getSurvey().getId());
        }
        return responseDto;
    }
}
