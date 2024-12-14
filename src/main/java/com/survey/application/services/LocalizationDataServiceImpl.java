package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;
import com.survey.domain.models.LocalizationData;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.repository.LocalizationDataRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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
    public List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtoList) {

        List<LocalizationData> entities = localizationDataDtoList.stream()
                .map(this::mapToEntity)
                .toList();

        List<LocalizationData> savedEntities = localizationDataRepository.saveAllAndFlush(entities);
        savedEntities.forEach(entityManager::refresh);


        return savedEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<ResponseLocalizationDto> getLocalizationData(OffsetDateTime from, OffsetDateTime to, UUID respondentId, UUID surveyId, boolean outsideResearchArea) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("The 'from' date must be before 'to' date.");
        }

        StringBuilder jpql = new StringBuilder("SELECT ld.* FROM localization_data ld ");
        jpql.append("WHERE 1=1 ");

        if (from != null) {
            jpql.append("AND ld.date_time >= :fromDate ");
        }
        if (to != null) {
            jpql.append("AND ld.date_time <= :toDate ");
        }
        if (respondentId != null) {
            jpql.append("AND ld.respondent_id = :respondentId ");
        }
        if (surveyId != null) {
            jpql.append("AND ld.participation_id IS NOT NULL ");
            jpql.append("AND EXISTS (SELECT 1 FROM survey_participation sp WHERE sp.id = ld.participation_id AND sp.survey_id = :surveyId) ");
        }
        if (outsideResearchArea) {
            jpql.append("\nAND EXISTS (")
                    .append("SELECT 1 ")
                    .append("FROM (")
                    .append("    SELECT geography::::STGeomFromText('POLYGON((' + ")
                    .append("    (SELECT STRING_AGG(CAST(longitude AS NVARCHAR(20)) + ' ' + CAST(latitude AS NVARCHAR(20)), ', ') ")
                    .append("""
                             WITHIN GROUP (ORDER BY [order])\s
                            + ', ' +
                                (SELECT CAST(longitude AS NVARCHAR(20)) + ' ' + CAST(latitude AS NVARCHAR(20))
                                 FROM research_area
                                 WHERE [order] = (SELECT MIN([order]) FROM research_area))
                            FROM research_area)""")
                    .append("    + '))', 4326).MakeValid() AS area_geography ")
                    .append(") AS polygon ")
                    .append("WHERE polygon.area_geography.STContains(geography::::Point(CAST(ld.latitude AS NVARCHAR(20)), CAST(ld.longitude AS NVARCHAR(20)), 4326)) = 0")
                    .append(")");
        }

        jpql.append("ORDER BY ld.date_time");

        Query query = entityManager.createNativeQuery(jpql.toString(), LocalizationData.class);

        if (from != null) {
            query.setParameter("fromDate", from);
        }
        if (to != null) {
            query.setParameter("toDate", to);
        }
        if (respondentId != null) {
            query.setParameter("respondentId", respondentId);
        }
        if (surveyId != null) {
            query.setParameter("surveyId", surveyId);
        }

        List<LocalizationData> dbEntityList = query.getResultList();

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
