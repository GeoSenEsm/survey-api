package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.LocalizationData;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.repository.LocalizationDataRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocalizationDataServiceImpl implements LocalizationDataService{

    private final LocalizationDataRepository localizationDataRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final ModelMapper modelMapper;
    private final GeometryFactory geometryFactory;
    private final ClaimsPrincipalService claimsPrincipalService;

    public LocalizationDataServiceImpl(LocalizationDataRepository localizationDataRepository, SurveyParticipationRepository surveyParticipationRepository, ModelMapper modelMapper, GeometryFactory geometryFactory, ClaimsPrincipalService claimsPrincipalService) {
        this.localizationDataRepository = localizationDataRepository;
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.modelMapper = modelMapper;
        this.geometryFactory = geometryFactory;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @Override
    @Transactional
    public List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtos) {

        validateSurveyParticipationIdss(localizationDataDtos);

        List<LocalizationData> entities = localizationDataDtos.stream()
                .map(this::mapToEntity)
                .toList();

        List<LocalizationData> savedEntities = localizationDataRepository.saveAllAndFlush(entities);

        return savedEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

    private void validateSurveyParticipationIdss(List<LocalizationDataDto> dtos){
        UUID respondentId = claimsPrincipalService.findIdentityUser().getId();

        for (LocalizationDataDto dto : dtos){
            UUID surveyParticipationId = dto.getSurveyParticipationId();
            if (surveyParticipationId != null && !validateSurveyParticipationId(surveyParticipationId, respondentId)){
                throw new IllegalArgumentException("Invalid surveyParticipation ID or mismatched respondent for ID: " + surveyParticipationId);
            }
        }
    }

    private boolean validateSurveyParticipationId(UUID surveyParticipationId, UUID respondentId){
        return surveyParticipationRepository.findByIdAndRespondentId(surveyParticipationId, respondentId).isPresent();
    }

    private LocalizationData mapToEntity(LocalizationDataDto dto){
        LocalizationData entity = modelMapper.map(dto, LocalizationData.class);

        entity.setIdentityUser(claimsPrincipalService.findIdentityUser());

        if (dto.getSurveyParticipationId() != null) {
            SurveyParticipation surveyParticipation = surveyParticipationRepository
                    .findById(dto.getSurveyParticipationId())
                    .orElse(null);
            entity.setSurveyParticipation(surveyParticipation);
        }

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

    @Override
    public List<ResponseLocalizationDto> getLocalizationData(OffsetDateTime from, OffsetDateTime to) {
        if (from.isAfter(to)){
            throw new IllegalArgumentException("The 'from' date must be before 'to' date.");
        }

        List<LocalizationData> dbEntityList = localizationDataRepository.findAllBetween(from, to);

        return dbEntityList.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<ResponseLocalizationDto> getLocalizationDataForRespondent(UUID respondentId) {
        List<LocalizationData> localizationDataList = localizationDataRepository.findByRespondentId(respondentId);

        if (localizationDataList.isEmpty()){
            return null;
        }

        return localizationDataList.stream()
                .map(this::mapToDto)
                .toList();
    }

}
