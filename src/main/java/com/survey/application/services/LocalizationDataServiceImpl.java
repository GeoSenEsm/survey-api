package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.LocalizationData;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.repository.LocalizationDataRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
    @Transactional(readOnly = true)
    public List<ResponseLocalizationDto> getLocalizationData(OffsetDateTime dateFrom, OffsetDateTime dateTo, UUID identityUserId, UUID surveyId, Boolean outsideResearchArea) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Build query for data
        CriteriaQuery<LocalizationData> cq = cb.createQuery(LocalizationData.class);
        Root<LocalizationData> root = cq.from(LocalizationData.class);

        // Add fetch joins to eagerly load associations and avoid N+1 queries
        root.fetch("surveyParticipation", JoinType.LEFT);
        root.fetch("identityUser", JoinType.INNER);

        List<Predicate> predicates = buildPredicates(cb, root, dateFrom, dateTo, identityUserId, surveyId, outsideResearchArea);

        cq.select(root)
                .distinct(true)  // IMPORTANT: Prevents Hibernate from generating massive IN clause
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .orderBy(cb.asc(root.get("dateTime")));

        // Create typed query - no pagination, fetch all results
        TypedQuery<LocalizationData> query = entityManager.createQuery(cq);

        // Add query hints for better performance with large result sets
        query.setHint("org.hibernate.fetchSize", 1000);
        query.setHint("org.hibernate.readOnly", true);
        query.setHint("org.hibernate.cacheable", false);

        List<LocalizationData> dbEntityList = query.getResultList();

        return dbEntityList.stream()
                .map(this::mapToDto)
                .toList();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<LocalizationData> root,
                                           OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                           UUID identityUserId, UUID surveyId, Boolean outsideResearchArea) {
        List<Predicate> predicates = new ArrayList<>();

        // Date filter - ALWAYS present (most selective)
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

        // Survey ID filter - second most common
        if (surveyId != null) {
            predicates.add(cb.equal(root.get("surveyParticipation").get("survey").get("id"), surveyId));
        }

        // Identity user filter - optional
        if(identityUserId != null){
            predicates.add(cb.equal(root.get("identityUser").get("id"), identityUserId));
        }

        // Outside research area filter - optional
        if (outsideResearchArea != null){
            predicates.add(cb.equal(root.get("outsideResearchArea"), outsideResearchArea));
        }

        return predicates;
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
