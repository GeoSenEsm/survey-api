package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.LocalizationData;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.repository.LocalizationDataRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocalizationDataServiceImpl implements LocalizationDataService{

    private final LocalizationDataRepository localizationDataRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final ModelMapper modelMapper;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final EntityManager entityManager;

    @Autowired
    public LocalizationDataServiceImpl(LocalizationDataRepository localizationDataRepository, SurveyParticipationRepository surveyParticipationRepository, ModelMapper modelMapper, ClaimsPrincipalService claimsPrincipalService, EntityManager entityManager) {
        this.localizationDataRepository = localizationDataRepository;
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.modelMapper = modelMapper;
        this.claimsPrincipalService = claimsPrincipalService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtos, String token) {

        validateSurveyParticipationIdss(localizationDataDtos);

        List<LocalizationData> entities = localizationDataDtos.stream()
                .map(this::mapToEntity)
                .toList();

        List<LocalizationData> savedEntities = localizationDataRepository.saveAllAndFlush(entities);
        savedEntities.forEach(entityManager::refresh);


        return savedEntities.stream()
                .map(this::mapToDto)
                .toList();
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
            return List.of();
        }

        return localizationDataList.stream()
                .map(this::mapToDto)
                .toList();
    }

    private void validateSurveyParticipationIdss(List<LocalizationDataDto> dtos){
        UUID respondentId = claimsPrincipalService.findIdentityUser().getId();

        for (LocalizationDataDto dto : dtos){
            UUID surveyParticipationId = dto.getSurveyParticipationId();
            if (surveyParticipationId != null && !validateSurveyParticipationId(surveyParticipationId, respondentId)){
                throw new IllegalArgumentException("Invalid surveyParticipationId or mismatched respondentId for surveyParticipationId: " + surveyParticipationId);
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

        return entity;
    }

    private ResponseLocalizationDto mapToDto(LocalizationData entity){
        ResponseLocalizationDto responseDto = modelMapper.map(entity, ResponseLocalizationDto.class);

        if (entity.getSurveyParticipation() != null){
            responseDto.setSurveyParticipationId(entity.getSurveyParticipation().getId());
        }
        responseDto.setRespondentId(entity.getIdentityUser().getId());
        return responseDto;
    }




}
