package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.LocalizationData;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.repository.LocalizationDataRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    public List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtoList) {

        List<LocalizationData> entities = localizationDataDtoList.stream()
                .map(this::mapToEntity)
                .toList();

        List<LocalizationData> filteredEntities = entities.stream()
                .filter(entity -> entity.getSurveyParticipation() == null || !localizationDataRepository.existsByRespondentIdAndParticipationId(
                        entity.getIdentityUser().getId(), entity.getSurveyParticipation().getId()))
                .toList();

        List<LocalizationData> savedEntities = localizationDataRepository.saveAllAndFlush(filteredEntities);
        savedEntities.forEach(entityManager::refresh);


        return savedEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<ResponseLocalizationDto> getLocalizationData(OffsetDateTime dateFrom, OffsetDateTime dateTo, UUID identityUserId, UUID surveyId, Boolean outsideResearchArea) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LocalizationData> cq = cb.createQuery(LocalizationData.class);

        Root<LocalizationData> root = cq.from(LocalizationData.class);
        root.fetch("surveyParticipation", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        if (surveyId != null) {
            predicates.add(cb.equal(root.get("surveyParticipation").get("survey").get("id"), surveyId));
        }

        if(identityUserId != null){
            predicates.add(cb.equal(root.get("identityUser").get("id"), identityUserId));
        }

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

        if (outsideResearchArea != null){
            if (outsideResearchArea == Boolean.TRUE){
                predicates.add(cb.equal(root.get("outsideResearchArea"), Boolean.TRUE));
            }
            else {
                predicates.add(cb.equal(root.get("outsideResearchArea"), Boolean.FALSE));
            }
        }

        cq.select(root)
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .orderBy(cb.asc(root.get("dateTime")));

        List<LocalizationData> dbEntityList = entityManager.createQuery(cq)
                .getResultList();

        return dbEntityList.stream()
                .map(this::mapToDto)
                .toList();
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
            responseDto.setSurveyId(entity.getSurveyParticipation().getSurvey().getId());
        }
        responseDto.setRespondentId(entity.getIdentityUser().getId());
        return responseDto;
    }
}
