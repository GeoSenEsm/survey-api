package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.*;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveySectionRepository surveySectionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final RespondentGroupRepository respondentGroupRepository;
    private final SectionToUserGroupRepository sectionToUserGroupRepository;
    private final ModelMapper modelMapper;
    private final OrderValidationService orderValidationService;
    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, SurveySectionRepository surveySectionRepository,
                             QuestionRepository questionRepository, OptionRepository optionRepository,
                             RespondentGroupRepository respondentGroupRepository,
                             SectionToUserGroupRepository sectionToUserGroupRepository,
                             ModelMapper modelMapper, OrderValidationService orderValidationService, EntityManager entityManager) {
        this.surveyRepository = surveyRepository;
        this.surveySectionRepository = surveySectionRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.respondentGroupRepository = respondentGroupRepository;
        this.sectionToUserGroupRepository = sectionToUserGroupRepository;
        this.modelMapper = modelMapper;
        this.orderValidationService = orderValidationService;
        this.entityManager = entityManager;
    }

    @Override
    public ResponseSurveyDto createSurvey(CreateSurveyDto createSurveyDto) {
        if (!orderValidationService.validateOrders(createSurveyDto.getSurveySections())) {
            throw new IllegalArgumentException("Order of sections/questions/options not correct!");
        }

        Survey survey = saveSurvey(createSurveyDto);

        ResponseSurveyDto responseDto = modelMapper.map(survey, ResponseSurveyDto.class);

        List<ResponseSurveySectionDto> responseSections = new ArrayList<>();
        for (CreateSurveySectionDto createSectionDto : createSurveyDto.getSurveySections()) {
            SurveySection section = saveSurveySection(createSectionDto, survey);
            handleRespondentGroup(createSectionDto, section);

            List<ResponseQuestionDto> responseQuestions = saveQuestions(createSectionDto, section);

            ResponseSurveySectionDto responseSurveySectionDto = modelMapper.map(section, ResponseSurveySectionDto.class);
            responseSurveySectionDto.setQuestions(responseQuestions);

            responseSections.add(responseSurveySectionDto);
        }
        responseDto.setSurveySections(responseSections);
        return responseDto;
    }

    private Survey saveSurvey(CreateSurveyDto createSurveyDto) {
        Survey survey = new Survey();
        survey.setName(createSurveyDto.getName());

        Survey dbSurvey = surveyRepository.saveAndFlush(survey);
        entityManager.refresh(dbSurvey);

        return dbSurvey;
    }

    private SurveySection saveSurveySection(CreateSurveySectionDto createSectionDto, Survey survey) {
        SurveySection section = new SurveySection();
        section.setOrder(createSectionDto.getOrder());
        section.setName(createSectionDto.getName());
        section.setVisibility(Visibility.valueOf(createSectionDto.getVisibility()));
        section.setSurvey(survey);

        SurveySection dbSection = surveySectionRepository.saveAndFlush(section);
        entityManager.refresh(dbSection);

        return dbSection;
    }

    private void handleRespondentGroup(CreateSurveySectionDto createSectionDto, SurveySection section) {
        if (createSectionDto.getGroupId() == null) {
            if (createSectionDto.getVisibility().equals(Visibility.group_specific.name())) {
                throw new IllegalArgumentException("Setting visibility as group_specific must be followed by giving groupId.");
            }
            return;
        }

        if (!createSectionDto.getVisibility().equals(Visibility.group_specific.name())) {
            throw new IllegalArgumentException("Set section visibility to group_specific or remove groupId.");
        }

        Optional<RespondentGroup> optionalRespondentGroup = respondentGroupRepository.findById(UUID.fromString(createSectionDto.getGroupId()));
        if (optionalRespondentGroup.isEmpty()) {
            throw new NoSuchElementException("Respondent group not found for ID: " + createSectionDto.getGroupId());
        }

        SectionToUserGroup sectionToUserGroup = new SectionToUserGroup();
        sectionToUserGroup.setSection(section);
        sectionToUserGroup.setGroup(optionalRespondentGroup.get());
        sectionToUserGroupRepository.save(sectionToUserGroup);
    }


    private List<ResponseQuestionDto> saveQuestions(CreateSurveySectionDto createSectionDto, SurveySection section) {
        List<ResponseQuestionDto> responseQuestions = new ArrayList<>();
        for (CreateQuestionDto createQuestionDto : createSectionDto.getQuestions()) {
            Question question = saveQuestion(createQuestionDto, section);
            List<ResponseOptionDto> responseOptions = saveOptions(createQuestionDto, question);
            ResponseQuestionDto responseQuestionDto = modelMapper.map(question, ResponseQuestionDto.class);
            responseQuestionDto.setOptions(responseOptions);
            responseQuestions.add(responseQuestionDto);
        }

        return responseQuestions;
    }

    private Question saveQuestion(CreateQuestionDto createQuestionDto, SurveySection section) {
        Question question = new Question();
        question.setOrder(createQuestionDto.getOrder());
        question.setContent(createQuestionDto.getContent());
        question.setQuestionType(QuestionType.valueOf(createQuestionDto.getQuestionType()));
        question.setRequired(createQuestionDto.isRequired());
        question.setSection(section);

        Question dbQuestion = questionRepository.saveAndFlush(question);
        entityManager.refresh(dbQuestion);

        return dbQuestion;
    }


    private List<ResponseOptionDto> saveOptions(CreateQuestionDto createQuestionDto, Question question) {
        List<ResponseOptionDto> responseOptions = new ArrayList<>();
        for (CreateOptionDto createOptionDto : createQuestionDto.getOptions()) {
            Option option = new Option();
            option.setOrder(createOptionDto.getOrder());
            option.setLabel(createOptionDto.getLabel());
            option.setQuestion(question);
            Option dbOption = optionRepository.saveAndFlush(option); entityManager.refresh(dbOption);

            responseOptions.add(modelMapper.map(dbOption, ResponseOptionDto.class));
        }
        return responseOptions;
    }

}
