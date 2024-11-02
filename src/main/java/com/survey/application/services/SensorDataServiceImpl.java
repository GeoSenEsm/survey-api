package com.survey.application.services;

import com.survey.application.dtos.ResponseSensorDataDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorData;
import com.survey.domain.repository.SensorDataRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class SensorDataServiceImpl implements SensorDataService {
    private final ClaimsPrincipalService claimsPrincipalService;
    private final ModelMapper modelMapper;
    private final SensorDataRepository sensorDataRepository;

    @Autowired
    public SensorDataServiceImpl(ClaimsPrincipalService claimsPrincipalService, ModelMapper modelMapper, SensorDataRepository sensorDataRepository) {
        this.claimsPrincipalService = claimsPrincipalService;
        this.modelMapper = modelMapper;
        this.sensorDataRepository = sensorDataRepository;
    }

    @Override
    public List<ResponseSensorDataDto> saveSensorData(String token, List<SensorDataDto> temperatureDataDtoList) {
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
    public List<ResponseSensorDataDto> getSensorData(OffsetDateTime from, OffsetDateTime to) {
        if (from.isAfter(to)){
            throw new IllegalArgumentException("The 'from' date must be before 'to' date.");
        }

        List<SensorData> dbEntityList = sensorDataRepository.findAllBetween(from, to);
        return mapToResponseDtoList(dbEntityList);
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
