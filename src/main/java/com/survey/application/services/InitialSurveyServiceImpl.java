package com.survey.application.services;

import com.survey.application.dtos.initialSurvey.*;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.SurveyState;
import com.survey.domain.repository.InitialSurveyRepository;
import com.survey.domain.repository.RespondentGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InitialSurveyServiceImpl implements InitialSurveyService {

    private final InitialSurveyRepository initialSurveyRepository;
    private final ModelMapper modelMapper;
    @PersistenceContext
    private final EntityManager entityManager;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final RespondentGroupRepository respondentGroupRepository;

    @Autowired
    public InitialSurveyServiceImpl(InitialSurveyRepository initialSurveyQuestionRepository, ModelMapper modelMapper, EntityManager entityManager, ClaimsPrincipalService claimsPrincipalService, RespondentGroupRepository respondentGroupRepository) {
        this.initialSurveyRepository = initialSurveyQuestionRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
        this.claimsPrincipalService = claimsPrincipalService;
        this.respondentGroupRepository = respondentGroupRepository;
    }

    @Override
    @Transactional
    public List<InitialSurveyQuestionResponseDto> createInitialSurvey(List<CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList) {
        if (isInitialSurveyPublished()){
            throw new IllegalStateException("Initial survey is already published.");
        }

        InitialSurvey initialSurvey = mapToInitialSurvey(createInitialSurveyQuestionDtoList);
        InitialSurvey dbInitialSurvey = initialSurveyRepository.saveAndFlush(initialSurvey);
        entityManager.refresh(dbInitialSurvey);
        return mapToInitialSurveyResponseDto(dbInitialSurvey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InitialSurveyQuestionResponseDto> getInitialSurvey() {
        IdentityUser identityUser = claimsPrincipalService.findIdentityUser();
        String userRole = identityUser.getRole();

        return switch (userRole) {
            case "Respondent" -> {
                if (isInitialSurveyPublished()) {
                    yield mapToInitialSurveyResponseDto(findInitialSurvey());
                }
                throw new NoSuchElementException("Initial survey not published yet.");
            }
            case "Admin" -> mapToInitialSurveyResponseDto(findInitialSurvey());
            default -> null;
        };
    }

    @Override
    public InitialSurveyStateDto checkInitialSurveyState() {
        InitialSurveyStateDto initialSurveyStateDto = new InitialSurveyStateDto();
        try {
            initialSurveyStateDto.setText(findInitialSurvey().getState().toString());
        } catch (NoSuchElementException e){
            initialSurveyStateDto.setText("not_created");
        }

        return initialSurveyStateDto;
    }

    @Override
    @Transactional
    public void publishInitialSurveyAndCreateRespondentGroups() {
        InitialSurvey initialSurvey = findInitialSurvey();

        if (initialSurvey.getState() == SurveyState.published){
            throw new IllegalStateException("Initial survey is already published.");
        }
        initialSurvey.setState(SurveyState.published);
        initialSurveyRepository.saveAndFlush(initialSurvey);

        createRespondentGroups(initialSurvey);
    }

    private void createRespondentGroups(InitialSurvey initialSurvey){
        List<RespondentGroup> respondentGroups = initialSurvey.getQuestions().stream()
                        .flatMap(question -> question.getOptions().stream()
                                .map(option -> {
                                    RespondentGroup respondentGroup = new RespondentGroup();
                                    respondentGroup.setName(question.getContent() + " - " + option.getContent());
                                    return respondentGroup;
                                })
                        ).toList();

        respondentGroupRepository.saveAll(respondentGroups);
    }

    private boolean isInitialSurveyPublished(){
        Optional<InitialSurvey> optionalInitialSurvey = initialSurveyRepository.findTopByRowVersionDesc();
        return optionalInitialSurvey.filter(initialSurvey -> initialSurvey.getState() == SurveyState.published).isPresent();
    }

    private InitialSurvey findInitialSurvey() {
        return initialSurveyRepository.findTopByRowVersionDesc()
                .orElseThrow(() -> new NoSuchElementException("No initial survey created"));
    }

    private List<InitialSurveyQuestionResponseDto> mapToInitialSurveyResponseDto(InitialSurvey initialSurvey) {
        return initialSurvey.getQuestions().stream()
                .sorted(Comparator.comparing(InitialSurveyQuestion::getOrder))
                .map(this::mapToInitialSurveyQuestionResponseDto)
                .collect(Collectors.toList());
    }

    private InitialSurveyQuestionResponseDto mapToInitialSurveyQuestionResponseDto(InitialSurveyQuestion question) {
        InitialSurveyQuestionResponseDto questionDto = modelMapper.map(question, InitialSurveyQuestionResponseDto.class);
        questionDto.setOptions(
                question.getOptions().stream()
                        .sorted(Comparator.comparing(InitialSurveyOption::getOrder))
                        .map(this::mapToInitialSurveyOptionResponseDto)
                        .collect(Collectors.toList()));
        return questionDto;
    }

    private InitialSurveyOptionResponseDto mapToInitialSurveyOptionResponseDto(InitialSurveyOption option) {
        return modelMapper.map(option, InitialSurveyOptionResponseDto.class);
    }

    private InitialSurvey mapToInitialSurvey(List<CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList) {
        InitialSurvey initialSurvey = new InitialSurvey();
        List<InitialSurveyQuestion> questions = createInitialSurveyQuestionDtoList.stream()
                .map(this::mapToSurveyQuestion)
                .peek(question -> question.setInitialSurvey(initialSurvey))
                .collect(Collectors.toList());
        initialSurvey.setQuestions(questions);
        initialSurvey.setState(SurveyState.created);

        return initialSurvey;
    }

    private InitialSurveyQuestion mapToSurveyQuestion(CreateInitialSurveyQuestionDto questionDto) {
        InitialSurveyQuestion question = new InitialSurveyQuestion();
        question.setOrder(questionDto.getOrder());
        question.setContent(questionDto.getContent());

        List<InitialSurveyOption> options = mapToSurveyOptions(questionDto.getOptions(), question);
        question.setOptions(options);
        return question;
    }

    private List<InitialSurveyOption> mapToSurveyOptions(List<CreateInitialSurveyOptionDto> optionDtoList, InitialSurveyQuestion question) {
        return optionDtoList.stream()
                .map(optionDto -> mapToSurveyOption(optionDto, question))
                .collect(Collectors.toList());
    }

    private InitialSurveyOption mapToSurveyOption(CreateInitialSurveyOptionDto optionDto, InitialSurveyQuestion question) {
        InitialSurveyOption option = new InitialSurveyOption();
        option.setOrder(optionDto.getOrder());
        option.setContent(optionDto.getContent());
        option.setQuestion(question);
        return option;
    }
}
