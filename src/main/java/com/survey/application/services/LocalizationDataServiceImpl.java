package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.LocalizationData;
import com.survey.domain.repository.LocalizationDataRepository;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LocalizationDataServiceImpl implements LocalizationDataService{

    private final LocalizationDataRepository localizationDataRepository;
    private final ModelMapper modelMapper;
    private final GeometryFactory geometryFactory;
    private final ClaimsPrincipalService claimsPrincipalService;

    public LocalizationDataServiceImpl(LocalizationDataRepository localizationDataRepository, ModelMapper modelMapper, GeometryFactory geometryFactory, ClaimsPrincipalService claimsPrincipalService) {
        this.localizationDataRepository = localizationDataRepository;
        this.modelMapper = modelMapper;
        this.geometryFactory = geometryFactory;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @Override
    @Transactional
    public List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtos) {
        List<LocalizationData> entities = localizationDataDtos.stream()
                .map(this::mapToEntity)
                .toList();

        List<LocalizationData> savedEntities = localizationDataRepository.saveAllAndFlush(entities);

        return savedEntities.stream()
                .map(this::mapToDto)
                .toList();

    }

    private LocalizationData mapToEntity(LocalizationDataDto dto){
        LocalizationData entity = modelMapper.map(dto, LocalizationData.class);

        entity.setIdentityUser(claimsPrincipalService.findIdentityUser());
        entity.setLocalization(createPoint(dto.getLongitude(), dto.getLatitude()));

        return entity;
    }

    private ResponseLocalizationDto mapToDto(LocalizationData entity){
        ResponseLocalizationDto responseDto = modelMapper.map(entity, ResponseLocalizationDto.class);

        if (entity.getSurveyParticipation() != null){
            responseDto.setSurveyParticipationId(entity.getSurveyParticipation().getId());
        }
        responseDto.setRespondentId(entity.getIdentityUser().getId());
        responseDto.setLongitude(BigDecimal.valueOf(entity.getLocalization().getX()));
        responseDto.setLatitude(BigDecimal.valueOf(entity.getLocalization().getY()));

        return responseDto;
    }

    private Point createPoint(BigDecimal longitude, BigDecimal latitude){
        Coordinate coordinate = new Coordinate(longitude.doubleValue(), latitude.doubleValue());
        return geometryFactory.createPoint(coordinate);
    }

}
