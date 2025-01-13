package com.survey.application.services;

import com.survey.application.dtos.LastSensorEntryDateDto;
import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorData;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SensorDataRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class SensorDataServiceImpl implements SensorDataService {
    private final ClaimsPrincipalService claimsPrincipalService;
    private final ModelMapper modelMapper;
    private final SensorDataRepository sensorDataRepository;
    private final IdentityUserRepository identityUserRepository;
    private final EntityManager entityManager;

    @Autowired
    public SensorDataServiceImpl(ClaimsPrincipalService claimsPrincipalService, ModelMapper modelMapper, SensorDataRepository sensorDataRepository, IdentityUserRepository identityUserRepository, EntityManager entityManager) {
        this.claimsPrincipalService = claimsPrincipalService;
        this.modelMapper = modelMapper;
        this.sensorDataRepository = sensorDataRepository;
        this.identityUserRepository = identityUserRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<ResponseSensorDataDto> saveSensorData(List<SensorDataDto> temperatureDataDtoList) {
        if (temperatureDataDtoList == null || temperatureDataDtoList.isEmpty()){
            throw new IllegalArgumentException("Temperature data list cannot be empty.");
        }

        IdentityUser identityUser = claimsPrincipalService.findIdentityUser();

        List<SensorData> entityList = temperatureDataDtoList.stream()
                        .map(dto -> {
                            SensorData entity = modelMapper.map(dto, SensorData.class);
                            entity.setRespondent(identityUser);
                            return entity;
                        })
                        .toList();
        List<SensorData> dbEntityList = sensorDataRepository.saveAll(entityList);

        return mapToResponseDtoList(dbEntityList);
    }

    @Override
    public List<ResponseSensorDataDto> getSensorData(OffsetDateTime dateFrom, OffsetDateTime dateTo, UUID identityUserId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SensorData> cq = cb.createQuery(SensorData.class);
        Root<SensorData> root = cq.from(SensorData.class);

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

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));

        List<SensorData> sensorDataList = entityManager.createQuery(cq).getResultList();

        return mapToResponseDtoList(sensorDataList);
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

    private List<ResponseSensorDataDto> mapToResponseDtoList (List<SensorData> entityList){
        return entityList.stream()
                .map(entity -> {
                    ResponseSensorDataDto responseDto = modelMapper.map(entity, ResponseSensorDataDto.class);
                    responseDto.setRespondentId(entity.getRespondent().getId());
                    return responseDto;
                }).toList();
    }
}
