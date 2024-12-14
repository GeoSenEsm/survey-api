package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.RespondentFilterOption;
import com.survey.domain.models.enums.SurveyState;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequestScope
public class RespondentDataServiceImpl implements RespondentDataService{
    private final RespondentDataRepository respondentDataRepository;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final EntityManager entityManager;
    private final InitialSurveyRepository initialSurveyRepository;
    private final InitialSurveyQuestionRepository initialSurveyQuestionRepository;
    private final InitialSurveyOptionRepository initialSurveyOptionRepository;
    private final RespondentGroupRepository respondentGroupRepository;
    private final RespondentToGroupRepository respondentToGroupRepository;
    private final IdentityUserRepository identityUserRepository;



    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository,
                                     ClaimsPrincipalService claimsPrincipalService, EntityManager entityManager, InitialSurveyRepository initialSurveyRepository, InitialSurveyQuestionRepository initialSurveyQuestionRepository, InitialSurveyOptionRepository initialSurveyOptionRepository, RespondentGroupRepository respondentGroupRepository, RespondentToGroupRepository respondentToGroupRepository, IdentityUserRepository identityUserRepository) {
        this.respondentDataRepository = respondentDataRepository;
        this.claimsPrincipalService = claimsPrincipalService;
        this.entityManager = entityManager;
        this.initialSurveyRepository = initialSurveyRepository;
        this.initialSurveyQuestionRepository = initialSurveyQuestionRepository;
        this.initialSurveyOptionRepository = initialSurveyOptionRepository;
        this.respondentGroupRepository = respondentGroupRepository;
        this.respondentToGroupRepository = respondentToGroupRepository;
        this.identityUserRepository = identityUserRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createRespondent(List<CreateRespondentDataDto> dtoList)
            throws BadCredentialsException, InvalidAttributeValueException, InstanceAlreadyExistsException, BadRequestException {
        IdentityUser identityUser = claimsPrincipalService.findIdentityUser();
        checkIfRespondentDataExists(identityUser.getId());
        validateCreateRespondentDataDtoList(dtoList);

        RespondentData respondentData = initializeRespondentData(identityUser);
        respondentData.setRespondentDataQuestions(createRespondentDataQuestions(dtoList, respondentData));
        RespondentData savedRespondentData = respondentDataRepository.save(respondentData);

        saveRespondentToGroupEntities(dtoList, savedRespondentData);
        return mapRespondentDataToResponse(savedRespondentData);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getAll(RespondentFilterOption filterOption, Integer amount, OffsetDateTime from, OffsetDateTime to){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IdentityUser> cq = cb.createQuery(IdentityUser.class);
        Root<IdentityUser> identityUserRoot = cq.from(IdentityUser.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(identityUserRoot.get("role"), "Respondent"));

        if(filterOption != null){
            predicates.add(buildFilterPredicate(cb, cq, identityUserRoot, filterOption, amount, from, to));
        }

        cq.select(identityUserRoot).where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapResultToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> getFromUserContext(){
        UUID currentUserId = claimsPrincipalService.findIdentityUser().getId();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RespondentData> cq = cb.createQuery(RespondentData.class);
        Root<RespondentData> respondentData = cq.from(RespondentData.class);
        Predicate condition = cb.equal(respondentData.get("identityUserId"), currentUserId);
        cq.where(condition);
        cq.select(respondentData);

        RespondentData dbRespondentData = entityManager
                .createQuery(cq)
                .getResultStream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        return mapRespondentDataToResponse(dbRespondentData);
    }

    @Override
    @Transactional
    public Map<String, Object> updateRespondent(List<CreateRespondentDataDto> dtoList, UUID identityUserId) {
        IdentityUser identityUser = identityUserRepository.findById(identityUserId)
                .orElseThrow(() -> new IllegalArgumentException("Respondent with given identity user id not found"));

        RespondentData respondentData = respondentDataRepository.findByIdentityUserId(identityUserId);

        validateCreateRespondentDataDtoList(dtoList);

        if (respondentData != null) {
            clearExistingRespondentData(respondentData);
        }
        respondentData = initializeRespondentData(identityUser);

        respondentData.setRespondentDataQuestions(createRespondentDataQuestions(dtoList, respondentData));

        RespondentData savedRespondentData = respondentDataRepository.save(respondentData);
        saveRespondentToGroupEntities(dtoList, savedRespondentData);

        return mapRespondentDataToResponse(savedRespondentData);
    }

    private void validateCreateRespondentDataDtoList(List<CreateRespondentDataDto> dtoList) {
        InitialSurvey initialSurvey = initialSurveyRepository.findTopByRowVersionDesc()
                .orElseThrow(() -> new IllegalStateException("Initial survey does not exist yet."));

        if (initialSurvey.getState() != SurveyState.published) {
            throw new IllegalStateException("Initial survey is not published yet.");
        }

        if (initialSurvey.getQuestions().size() != dtoList.size()) {
            throw new IllegalArgumentException("The number of questions in the initial survey does not match the number of provided DTOs.");
        }
    }

    private void clearExistingRespondentData(RespondentData respondentData){
        UUID respondentDataId = respondentData.getId();
        respondentToGroupRepository.deleteAllByRespondentDataId(respondentDataId);
        respondentDataRepository.deleteById(respondentDataId);
    }

    private Predicate buildFilterPredicate(CriteriaBuilder cb, CriteriaQuery<IdentityUser> cq, Root<IdentityUser> identityUserRoot, RespondentFilterOption filterOption, Integer amount, OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null || amount == null) {
            throw new IllegalArgumentException("'from', 'to' and 'amount' are required for filtering.");
        }
        return switch (filterOption) {
            case skipped_surveys -> buildSkippedSurveysPredicate(cb, cq, identityUserRoot, amount, from, to);
            case location_not_sent -> buildLocationNotSentPredicate(cb, cq, identityUserRoot, amount, from, to);
            case sensors_data_not_sent -> buildSensorsDataNotSentPredicate(cb, cq, identityUserRoot, amount, from, to);
        };
    }

    private Predicate buildSkippedSurveysPredicate(CriteriaBuilder cb, CriteriaQuery<IdentityUser> cq, Root<IdentityUser> identityUserRoot, Integer amount, OffsetDateTime from, OffsetDateTime to) {
        Subquery<UUID> activeSurveySubquery = createActiveSurveySubquery(cb, cq, from, to);
        Subquery<UUID> visibleSurveySubquery = createVisibleSurveySubquery(cb, cq, identityUserRoot);

        Subquery<Long> activeVisibleSurveysCountSubquery = cq.subquery(Long.class);
        Root<SurveySection> countRoot = activeVisibleSurveysCountSubquery.from(SurveySection.class);
        activeVisibleSurveysCountSubquery.select(cb.countDistinct(countRoot.get("survey").get("id")))
                .where(countRoot.get("survey").get("id").in(activeSurveySubquery),
                        countRoot.get("survey").get("id").in(visibleSurveySubquery));

        Subquery<Long> participationCountSubquery = cq.subquery(Long.class);
        Root<SurveyParticipation> participationRoot = participationCountSubquery.from(SurveyParticipation.class);
        participationCountSubquery.select(cb.count(participationRoot.get("id")))
                .where(cb.equal(participationRoot.get("identityUser").get("id"), identityUserRoot.get("id")),
                        participationRoot.get("survey").get("id").in(activeSurveySubquery));

        return cb.ge(cb.diff(activeVisibleSurveysCountSubquery, participationCountSubquery.getSelection()), amount.longValue());
    }
    private Predicate buildLocationNotSentPredicate(
            CriteriaBuilder cb,
            CriteriaQuery<?> cq,
            Root<IdentityUser> identityUserRoot,
            Integer amount,
            OffsetDateTime from,
            OffsetDateTime to) {

        OffsetDateTime adjustedFrom = from.plusDays(1);
        OffsetDateTime adjustedTo = to.plusDays(1);

        Subquery<Long> localizationCountSubquery = cq.subquery(Long.class);
        Root<LocalizationData> localizationRoot = localizationCountSubquery.from(LocalizationData.class);
        localizationCountSubquery.select(cb.count(localizationRoot.get("id")))
                .where(cb.equal(localizationRoot.get("identityUser").get("id"), identityUserRoot.get("id")),
                        cb.between(localizationRoot.get("dateTime"), adjustedFrom, adjustedTo));

        return cb.lessThanOrEqualTo(localizationCountSubquery.getSelection(), amount.longValue());
    }
    private Predicate buildSensorsDataNotSentPredicate(
            CriteriaBuilder cb,
            CriteriaQuery<?> cq,
            Root<IdentityUser> identityUserRoot,
            Integer amount,
            OffsetDateTime from,
            OffsetDateTime to) {

        Subquery<Long> sensorDataCountSubquery = cq.subquery(Long.class);
        Root<SensorData> sensorDataRoot = sensorDataCountSubquery.from(SensorData.class);
        sensorDataCountSubquery.select(cb.count(sensorDataRoot.get("id")))
                .where(cb.equal(sensorDataRoot.get("respondent").get("id"), identityUserRoot.get("id")),
                        cb.between(sensorDataRoot.get("dateTime"), from, to));

        return cb.lessThanOrEqualTo(sensorDataCountSubquery.getSelection(), amount.longValue());
    }
    private Subquery<UUID> createActiveSurveySubquery(CriteriaBuilder cb, CriteriaQuery<?> cq, OffsetDateTime from, OffsetDateTime to) {
        Subquery<UUID> activeSurveySubquery = cq.subquery(UUID.class);
        Root<SurveyParticipationTimeSlot> timeSlotRoot = activeSurveySubquery.from(SurveyParticipationTimeSlot.class);
        activeSurveySubquery.select(timeSlotRoot.get("surveySendingPolicy").get("survey").get("id"))
                .where(cb.and(cb.equal(timeSlotRoot.get("isDeleted"), false),
                        cb.lessThanOrEqualTo(timeSlotRoot.get("finish"), to),
                        cb.greaterThanOrEqualTo(timeSlotRoot.get("start"), from)));
        return activeSurveySubquery;
    }
    private Subquery<UUID> createVisibleSurveySubquery(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<IdentityUser> identityUserRoot) {
        Subquery<UUID> visibleSurveySubquery = cq.subquery(UUID.class);
        Root<SurveySection> surveySectionRoot = visibleSurveySubquery.from(SurveySection.class);

        Predicate alwaysVisible = cb.equal(surveySectionRoot.get("visibility"), Visibility.always);

        Join<SurveySection, SectionToUserGroup> sectionToGroupJoin = surveySectionRoot.join("sectionToUserGroups", JoinType.LEFT);
        Join<SectionToUserGroup, RespondentGroup> groupJoin = sectionToGroupJoin.join("group", JoinType.LEFT);
        Join<RespondentGroup, RespondentData> respondentDataJoin = groupJoin.join("respondentData", JoinType.LEFT);

        Predicate groupSpecificVisible = cb.and(
                cb.equal(surveySectionRoot.get("visibility"), Visibility.group_specific),
                respondentDataJoin.get("identityUserId").in(identityUserRoot.get("id")));

        Predicate sectionVisible = cb.or(alwaysVisible, groupSpecificVisible);
        visibleSurveySubquery.select(surveySectionRoot.get("survey").get("id")).where(sectionVisible);

        return visibleSurveySubquery;
    }
    private void saveRespondentToGroupEntities(List<CreateRespondentDataDto> createRespondentDataDtoList, RespondentData respondentData) {
        List<RespondentToGroup> respondentToGroupList = new ArrayList<>();

        for (CreateRespondentDataDto dto : createRespondentDataDtoList) {
            String questionContent = initialSurveyQuestionRepository.findById(dto.getQuestionId())
                    .map(InitialSurveyQuestion::getContent)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Question ID: " + dto.getQuestionId()));

            String optionContent = initialSurveyOptionRepository.findById(dto.getOptionId())
                    .map(InitialSurveyOption::getContent)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Option ID: " + dto.getOptionId()));

            String groupName = questionContent + " - " + optionContent;

            RespondentGroup rg = respondentGroupRepository.findByGroupName(groupName);
            if (rg == null) {
                throw new IllegalArgumentException("Group not found for name: " + groupName);
            }

            RespondentToGroup rtg = new RespondentToGroup();
            rtg.setRespondentData(respondentData);
            rtg.setRespondentGroup(rg);
            respondentToGroupList.add(rtg);
        }

        RespondentGroup respondentGroupAll = respondentGroupRepository.findByGroupName("All");
        if (respondentGroupAll == null) {
            throw new IllegalArgumentException("Default group 'All' not found");
        }

        RespondentToGroup rtgAll = new RespondentToGroup();
        rtgAll.setRespondentData(respondentData);
        rtgAll.setRespondentGroup(respondentGroupAll);
        respondentToGroupList.add(rtgAll);

        respondentToGroupRepository.saveAllAndFlush(respondentToGroupList);
    }

    private RespondentData initializeRespondentData(IdentityUser identityUser) {
        RespondentData respondentData = new RespondentData();
        respondentData.setIdentityUserId(identityUser.getId());
        respondentData.setIdentityUser(identityUser);
        respondentData.setInitialSurvey(getOrCreateInitialSurvey());
        return respondentData;
    }

    public InitialSurvey getOrCreateInitialSurvey() {
        return initialSurveyRepository.findTopByRowVersionDesc()
                .orElseGet(() -> {
                    InitialSurvey newSurvey = new InitialSurvey();
                    return initialSurveyRepository.save(newSurvey);
                });
    }

    private void checkIfRespondentDataExists(UUID userId) throws InstanceAlreadyExistsException {
        if (doesRespondentDataExist(userId)) {
            throw new InstanceAlreadyExistsException("Respondent data already exists for this user.");
        }
    }

    private List<RespondentDataQuestion> createRespondentDataQuestions(List<CreateRespondentDataDto> dto, RespondentData respondentData) {
        Map<UUID, InitialSurveyQuestion> questionMap = findQuestionsByIds(dto.stream()
                .map(CreateRespondentDataDto::getQuestionId)
                .collect(Collectors.toList()));

        Map<UUID, InitialSurveyOption> optionMap = findOptionsByIds(dto.stream()
                .map(CreateRespondentDataDto::getOptionId)
                .collect(Collectors.toList()));

        return dto.stream()
                .map(data -> buildRespondentDataQuestion(data, respondentData, questionMap, optionMap))
                .collect(Collectors.toList());
    }

    private RespondentDataQuestion buildRespondentDataQuestion(CreateRespondentDataDto dto, RespondentData respondentData,
                                                               Map<UUID, InitialSurveyQuestion> questionMap, Map<UUID, InitialSurveyOption> optionMap) {
        InitialSurveyQuestion question = questionMap.get(dto.getQuestionId());
        InitialSurveyOption option = optionMap.get(dto.getOptionId());

        if (question == null || option == null) {
            throw new IllegalArgumentException("Invalid question or option ID in provided data.");
        }

        RespondentDataQuestion respondentDataQuestion = new RespondentDataQuestion();
        respondentDataQuestion.setRespondentData(respondentData);
        respondentDataQuestion.setQuestion(question);

        RespondentDataOption optionSelection = new RespondentDataOption();
        optionSelection.setRespondentDataQuestions(respondentDataQuestion);
        optionSelection.setOption(option);
        respondentDataQuestion.setOptions(Collections.singletonList(optionSelection));

        return respondentDataQuestion;
    }

    private Map<UUID, InitialSurveyQuestion> findQuestionsByIds(List<UUID> questionIds) {
        List<InitialSurveyQuestion> questions = initialSurveyQuestionRepository.findAllById(questionIds);
        return questions.stream().collect(Collectors.toMap(InitialSurveyQuestion::getId, question -> question));
    }

    private Map<UUID, InitialSurveyOption> findOptionsByIds(List<UUID> optionIds) {
        List<InitialSurveyOption> options = initialSurveyOptionRepository.findAllById(optionIds);
        return options.stream().collect(Collectors.toMap(InitialSurveyOption::getId, option -> option));
    }

    private boolean doesRespondentDataExist(UUID userId) {
        return respondentDataRepository.existsByIdentityUserId(userId);
    }

    private Map<String, Object> mapRespondentDataToResponse(RespondentData respondentData) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", respondentData.getIdentityUser().getId());
        response.put("username", respondentData.getUsername());

        mapQuestionsAndOptions(respondentData, response);
   
        return response;
    }
    private Map<String, Object> mapResultToResponse(IdentityUser identityUser) {
        Map<String, Object> response = new LinkedHashMap<>();
        UUID identityUserId = identityUser.getId();

        response.put("id", identityUserId);
        response.put("username", identityUser.getUsername());

        RespondentData respondentData = respondentDataRepository.findByIdentityUserId(identityUserId);

        if (respondentData != null) {
            mapQuestionsAndOptions(respondentData, response);
        }
        return response;
    }
    private void mapQuestionsAndOptions(RespondentData respondentData, Map<String, Object> response) {
        respondentData.getRespondentDataQuestions().forEach(question -> {
            String questionContent = question.getQuestion().getContent();
            question.getOptions().forEach(option -> {
                UUID optionId = option.getOption().getId();
                response.put(questionContent, optionId);
            });
        });
    }
}