package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.dtos.RespondentDataAnswerDto;
import com.survey.application.dtos.RespondentDataDto;
import com.survey.domain.models.*;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;
    private final IdentityUserRepository identityUserRepository;
    private final InitialSurveyRepository initialSurveyRepository;
    private final InitialSurveyQuestionRepository initialSurveyQuestionRepository;
    private final InitialSurveyOptionRepository initialSurveyOptionRepository;



    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository,
                                     ClaimsPrincipalService claimsPrincipalService, ModelMapper modelMapper,
                                     IdentityUserRepository identityUserRepository , EntityManager entityManager, InitialSurveyRepository initialSurveyRepository, InitialSurveyQuestionRepository initialSurveyQuestionRepository, InitialSurveyOptionRepository initialSurveyOptionRepository) {
        this.respondentDataRepository = respondentDataRepository;
        this.claimsPrincipalService = claimsPrincipalService;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
        this.identityUserRepository = identityUserRepository;
        this.initialSurveyRepository = initialSurveyRepository;
        this.initialSurveyQuestionRepository = initialSurveyQuestionRepository;
        this.initialSurveyOptionRepository = initialSurveyOptionRepository;
    }

    @Override
    @Transactional
    public RespondentDataDto createRespondent(CreateRespondentDataDto dto, String tokenWithPrefix)
            throws BadCredentialsException, InvalidAttributeValueException, InstanceAlreadyExistsException, BadRequestException {
        UUID currentUserUUID = getCurrentUserUUID();

        checkIfRespondentDataExists(currentUserUUID);

        RespondentData respondentData = mapDtoToRespondentData(dto, currentUserUUID);
        InitialSurvey survey = findInitialSurveyById(dto.getSurveyId());
        respondentData.setSurveyId(survey);
        respondentData.setIdentityUser(claimsPrincipalService.findIdentityUser());
        respondentDataRepository.save(respondentData);

        List<RespondentDataQuestion> respondentDataQuestions = createRespondentDataQuestions(dto, respondentData);
        respondentData.setRespondentDataQuestions(respondentDataQuestions);

        RespondentData savedRespondentData = respondentDataRepository.save(respondentData);
        return mapRespondentDataToDto(savedRespondentData);
    }

    @Override
    @Transactional
    public List<RespondentDataDto> getAll(){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RespondentData> cq = cb.createQuery(RespondentData.class);
        Root<RespondentData> respondentData = cq.from(RespondentData.class);
        cq.select(respondentData);

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapRespondentDataToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RespondentDataDto getFromUserContext(){
        UUID currentUserId = claimsPrincipalService.findIdentityUser().getId();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RespondentData> cq = cb.createQuery(RespondentData.class);
        Root<RespondentData> respondentData = cq.from(RespondentData.class);
        Fetch<RespondentData, IdentityUser> identityUserFetch = respondentData.fetch("identityUser");
        Predicate condition = cb.equal(respondentData.get("identityUserId"), currentUserId);
        cq.where(condition);
        cq.select(respondentData);

        RespondentData dbRespondentData = entityManager
                .createQuery(cq)
                .getResultStream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        return mapRespondentDataToDto(dbRespondentData);
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

    private RespondentData mapDtoToRespondentData(CreateRespondentDataDto dto, UUID currentUserUUID) {
        RespondentData respondentData = modelMapper.map(dto, RespondentData.class);
        respondentData.setIdentityUserId(currentUserUUID);
        return respondentData;
    }

    private List<RespondentDataQuestion> createRespondentDataQuestions(CreateRespondentDataDto dto, RespondentData respondentData) {
        Map<UUID, InitialSurveyOption> optionsMap = findOptionsBySurveyId(getQuestionIds(dto));
        Map<UUID, InitialSurveyQuestion> questionMap = getQuestionsMap(dto.getAnswers(), respondentData.getSurveyId().getId());

        List<RespondentDataQuestion> respondentDataQuestions = new ArrayList<>();
        for (RespondentDataAnswerDto answerDto : dto.getAnswers()) {
            RespondentDataQuestion respondentDataQuestion = createRespondentDataQuestion(answerDto, respondentData, questionMap, optionsMap);
            respondentDataQuestions.add(respondentDataQuestion);
        }
        return respondentDataQuestions;
    }

    private List<UUID> getQuestionIds(CreateRespondentDataDto dto) {
        return dto.getAnswers().stream()
                .map(RespondentDataAnswerDto::getQuestionId)
                .collect(Collectors.toList());
    }

    private Map<UUID, InitialSurveyQuestion> getQuestionsMap(List<RespondentDataAnswerDto> answers, UUID surveyId) {
        List<UUID> questionIds = answers.stream()
                .map(RespondentDataAnswerDto::getQuestionId)
                .collect(Collectors.toList());
        List<InitialSurveyQuestion> questions = findQuestionsByIds(questionIds, surveyId);
        return questions.stream()
                .collect(Collectors.toMap(InitialSurveyQuestion::getId, question -> question));
    }

    private RespondentDataQuestion createRespondentDataQuestion(RespondentDataAnswerDto answerDto, RespondentData respondentData,
                                                                Map<UUID, InitialSurveyQuestion> questionMap, Map<UUID, InitialSurveyOption> optionsMap) {

        UUID questionId = answerDto.getQuestionId();
        InitialSurveyQuestion question = questionMap.get(questionId);

        RespondentDataQuestion respondentDataQuestion = new RespondentDataQuestion();
        respondentDataQuestion.setRespondentData(respondentData);
        respondentDataQuestion.setQuestion(question);

        UUID optionId = answerDto.getOptionId();
        InitialSurveyOption option = optionsMap.get(optionId);

        if (option == null) {
            throw new IllegalArgumentException("Invalid option ID: " + optionId);
        }

        RespondentDataOption optionSelection = new RespondentDataOption();
        optionSelection.setRespondentDataQuestions(respondentDataQuestion);
        optionSelection.setOption(option);
        respondentDataQuestion.setOptions(Collections.singletonList(optionSelection));

        return respondentDataQuestion;
    }

    private Map<UUID, InitialSurveyOption> findOptionsBySurveyId(List<UUID> questionIds) {
        return initialSurveyOptionRepository.findByQuestionIdIn(questionIds)
                .stream()
                .collect(Collectors.toMap(InitialSurveyOption::getId, option -> option));
    }

    private InitialSurvey findInitialSurveyById(UUID surveyId) {
        return initialSurveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid survey ID - survey doesn't exist"));
    }

    private List<InitialSurveyQuestion> findQuestionsByIds(List<UUID> questionIds, UUID surveyId) {
        return initialSurveyQuestionRepository.findAllByIds(surveyId, questionIds);
    }

    private boolean doesRespondentDataExist(UUID userId) {
        return respondentDataRepository.existsByIdentityUserId(userId);
    }

    private UUID getUserUUID(String username) {
        return identityUserRepository.findByUsername(username)
                .map(IdentityUser::getId)
                .orElse(null);
    }

    private RespondentDataDto mapRespondentDataToDto(RespondentData respondent) {
        RespondentDataDto dto = modelMapper.map(respondent, RespondentDataDto.class);
        if (respondent.getRespondentDataQuestions() != null) {
            List<RespondentDataAnswerDto> answerDtos = respondent.getRespondentDataQuestions().stream()
                    .flatMap(question -> question.getOptions().stream()
                            .map(option -> {
                                RespondentDataAnswerDto answerDto = new RespondentDataAnswerDto();
                                answerDto.setQuestionId(question.getId());
                                answerDto.setOptionId(option.getId());
                                return answerDto;
                            }))
                    .collect(Collectors.toList());
            dto.setAnswers(answerDtos);
        }
        return dto;
    }
}