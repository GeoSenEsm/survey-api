package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.models.*;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.coyote.BadRequestException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequestScope
public class RespondentDataServiceImpl implements RespondentDataService{
    private final RespondentDataRepository respondentDataRepository;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final EntityManager entityManager;
    private final IdentityUserRepository identityUserRepository;
    private final InitialSurveyRepository initialSurveyRepository;
    private final InitialSurveyQuestionRepository initialSurveyQuestionRepository;
    private final InitialSurveyOptionRepository initialSurveyOptionRepository;



    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository,
                                     ClaimsPrincipalService claimsPrincipalService, IdentityUserRepository identityUserRepository, EntityManager entityManager, InitialSurveyRepository initialSurveyRepository, InitialSurveyQuestionRepository initialSurveyQuestionRepository, InitialSurveyOptionRepository initialSurveyOptionRepository) {
        this.respondentDataRepository = respondentDataRepository;
        this.claimsPrincipalService = claimsPrincipalService;
        this.entityManager = entityManager;
        this.identityUserRepository = identityUserRepository;
        this.initialSurveyRepository = initialSurveyRepository;
        this.initialSurveyQuestionRepository = initialSurveyQuestionRepository;
        this.initialSurveyOptionRepository = initialSurveyOptionRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createRespondent(List<CreateRespondentDataDto> dto, String tokenWithPrefix)
            throws BadCredentialsException, InvalidAttributeValueException, InstanceAlreadyExistsException, BadRequestException {
        UUID currentUserUUID = getCurrentUserUUID();

        checkIfRespondentDataExists(currentUserUUID);

        RespondentData respondentData = initializeRespondentData(currentUserUUID);
        respondentData.setRespondentDataQuestions(createRespondentDataQuestions(dto, respondentData));
        RespondentData savedRespondentData = respondentDataRepository.save(respondentData);

        return mapRespondentDataToResponse(savedRespondentData);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getAll(){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RespondentData> cq = cb.createQuery(RespondentData.class);
        Root<RespondentData> respondentData = cq.from(RespondentData.class);
        cq.select(respondentData);

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapRespondentDataToResponse)
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

    private RespondentData initializeRespondentData(UUID userId) {
        RespondentData respondentData = new RespondentData();
        respondentData.setIdentityUserId(userId);
        respondentData.setIdentityUser(claimsPrincipalService.findIdentityUser());
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

    private UUID getCurrentUserUUID() {
        String usernameFromJwt = getUsernameFromJwt();
        return getUserUUID(usernameFromJwt);
    }

    private String getUsernameFromJwt() {
        return claimsPrincipalService.getCurrentUsernameIfExists();
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

    private UUID getUserUUID(String username) {
        return identityUserRepository.findByUsername(username)
                .map(IdentityUser::getId)
                .orElse(null);
    }

    private Map<String, Object> mapRespondentDataToResponse(RespondentData respondentData) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", respondentData.getId());
        response.put("username", respondentData.getUsername());

        respondentData.getRespondentDataQuestions().forEach(respondentDataQuestion -> {
            String questionContent = respondentDataQuestion.getQuestion().getContent();
            respondentDataQuestion.getOptions().forEach(respondentDataOption -> {
                UUID optionId = respondentDataOption.getOption().getId();
                response.put(questionContent, optionId);
            });
        });

        return response;
    }
}