package com.survey.application.services;

import com.survey.application.dtos.ResponseTemperatureDataEntryDto;
import com.survey.application.dtos.TemperatureDataEntryDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.TemperatureData;
import com.survey.domain.repository.TemperatureDataRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class TemperatureDataServiceImpl implements TemperatureDataService{
    private final ClaimsPrincipalService claimsPrincipalService;
    private final ModelMapper modelMapper;
    private final TemperatureDataRepository temperatureDataRepository;

    @Autowired
    public TemperatureDataServiceImpl(ClaimsPrincipalService claimsPrincipalService, ModelMapper modelMapper, TemperatureDataRepository temperatureDataRepository) {
        this.claimsPrincipalService = claimsPrincipalService;
        this.modelMapper = modelMapper;
        this.temperatureDataRepository = temperatureDataRepository;
    }

    @Override
    public List<ResponseTemperatureDataEntryDto> saveTemperatureData(String token, List<TemperatureDataEntryDto> temperatureDataDtoList) {
        if (temperatureDataDtoList == null || temperatureDataDtoList.isEmpty()){
            throw new IllegalArgumentException("Temperature data list cannot be empty.");
        }

        IdentityUser identityUser = claimsPrincipalService.findIdentityUser();

        List<TemperatureData> entityList = temperatureDataDtoList.stream()
                        .map(dto -> {
                            TemperatureData entity = modelMapper.map(dto, TemperatureData.class);
                            entity.setRespondent(identityUser);
                            return entity;
                        })
                        .toList();
        List<TemperatureData> dbEntityList = temperatureDataRepository.saveAll(entityList);

        return mapToResponseDtoList(dbEntityList);
    }

    @Override
    public List<ResponseTemperatureDataEntryDto> getTemperatureData(OffsetDateTime from, OffsetDateTime to) {
        if (from.isAfter(to)){
            throw new IllegalArgumentException("The 'from' date must be before 'to' date.");
        }

        List<TemperatureData> dbEntityList = temperatureDataRepository.findAllBetween(from, to);
        return mapToResponseDtoList(dbEntityList);
    }

    private List<ResponseTemperatureDataEntryDto> mapToResponseDtoList (List<TemperatureData> entityList){
        return entityList.stream()
                .map(entity -> {
                    ResponseTemperatureDataEntryDto responseDto = modelMapper.map(entity, ResponseTemperatureDataEntryDto.class);
                    responseDto.setRespondentId(entity.getRespondent().getId());
                    return responseDto;
                }).toList();
    }
}
