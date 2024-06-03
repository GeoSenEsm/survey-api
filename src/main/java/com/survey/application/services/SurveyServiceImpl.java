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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final ModelMapper modelMapper;
    private final RespondentGroupRepository respondentGroupRepository;
    private final SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository;
    @PersistenceContext
    private final EntityManager entityManager;
    private final ShowSectionWithOrderValidationService showSectionWithOrderValidationService;
    private final Dictionary<Integer, String> sectionOrderAndVisibilityDict = new Hashtable<>();

    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, ModelMapper modelMapper, RespondentGroupRepository respondentGroupRepository, EntityManager entityManager, SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository, ShowSectionWithOrderValidationService showSectionWithOrderValidationService) {
        this.surveyRepository = surveyRepository;
        this.modelMapper = modelMapper;
        this.respondentGroupRepository = respondentGroupRepository;
        this.entityManager = entityManager;
        this.surveyParticipationTimeSlotRepository = surveyParticipationTimeSlotRepository;
        this.showSectionWithOrderValidationService = showSectionWithOrderValidationService;
    }

    @Override
    public ResponseSurveyDto createSurvey(CreateSurveyDto createSurveyDto) {
        fillSectionOrderAndVisibilityDict(createSurveyDto);
        Survey surveyEntity = mapToSurvey(createSurveyDto);

        Survey dbSurvey = surveyRepository.saveAndFlush(surveyEntity);
        entityManager.refresh(dbSurvey);
        return modelMapper.map(dbSurvey, ResponseSurveyDto.class);
    }

    @Override
    public List<ResponseSurveyDto> getSurveysByCompletionDate(LocalDate completionDate) {
        OffsetDateTime startOfDay = completionDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endOfDay = completionDate.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

        List<SurveyParticipationTimeSlot> timeSlots = surveyParticipationTimeSlotRepository.findByFinishBetween(startOfDay, endOfDay);

        List<Survey> surveys = timeSlots.stream()
                .map(slot -> slot.getSurveySendingPolicy().getSurvey())
                .distinct()
                .toList();

        return surveys.stream()
                .map(survey -> modelMapper.map(survey, ResponseSurveyDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseSurveyShortDto> getSurveysShort() {
        return surveyRepository.findAll().stream()
                .map(survey -> modelMapper.map(survey, ResponseSurveyShortDto.class))
                .collect(Collectors.toList());
    }

    private void fillSectionOrderAndVisibilityDict(CreateSurveyDto createSurveyDto){
        createSurveyDto.getSections().forEach(sectionDto ->
                sectionOrderAndVisibilityDict.put(sectionDto.getOrder(), sectionDto.getVisibility())
        );
    }

    private Survey mapToSurvey(CreateSurveyDto createSurveyDto){
        Survey survey = new Survey();

        survey.setName(createSurveyDto.getName());
        survey.setSections(createSurveyDto.getSections().stream()
                .map(sectionDto -> mapToSurveySection(sectionDto, survey))
                .collect(Collectors.toList()));
        return survey;
    }

    private SurveySection mapToSurveySection(CreateSurveySectionDto sectionDto, Survey surveyEntity){
        SurveySection surveySection = modelMapper.map(sectionDto, SurveySection.class);
        surveySection.setSurvey(surveyEntity);

        SectionToUserGroup sectionToUserGroup = getSectionToUserGroup(sectionDto, surveySection);

        surveySection.setSectionToUserGroups(sectionToUserGroup != null ? List.of(sectionToUserGroup) : null);
        surveySection.setQuestions(sectionDto.getQuestions().stream()
                .map(questionDto -> mapToQuestion(questionDto, surveySection))
                .collect(Collectors.toList())
        );

        return surveySection;
    }

    private Question mapToQuestion(CreateQuestionDto questionDto, SurveySection surveySection){
        Question question = modelMapper.map(questionDto, Question.class);
        question.setSection(surveySection);

        if (question.getQuestionType().equals(QuestionType.single_text_selection)){
            if (questionDto.getOptions() == null){
                throw new IllegalArgumentException("Question type set as single_text_selection - must include a list of options in dto.");
            }
            question.setNumberRange(null);
            question.setOptions(questionDto.getOptions().stream()
                    .map(optionDto -> mapToOption(optionDto, question, surveySection.getOrder()))
                    .collect(Collectors.toList()));
        }

        if (question.getQuestionType().equals(QuestionType.discrete_number_selection)){
            if (questionDto.getNumberRange() == null){
                throw new IllegalArgumentException("Question type set as discrete_number_selection - must include number range in dto.");
            }
            question.setNumberRange(mapToNumberRange(questionDto.getNumberRange(), question));
            question.setOptions(null);
        }

        return question;
    }

    private NumberRange mapToNumberRange(CreateNumberRangeOptionDto numberRangeOptionDto, Question question){
        NumberRange numberRange = modelMapper.map(numberRangeOptionDto, NumberRange.class);
        numberRange.setQuestion(question);
        return numberRange;
    }

    private Option mapToOption(CreateOptionDto optionDto, Question question, int currentSectionOrder) {
        showSectionWithOrderValidationService.validateSectionExistsAndIsAnswerTriggered(optionDto.getShowSection(), sectionOrderAndVisibilityDict, currentSectionOrder);

        Option option = modelMapper.map(optionDto, Option.class);
        option.setQuestion(question);
        return option;
    }

    private SectionToUserGroup getSectionToUserGroup(CreateSurveySectionDto createSurveySectionDto, SurveySection surveySectionEntity){
        String groupId = createSurveySectionDto.getGroupId();
        if (groupId == null){
            if (createSurveySectionDto.getVisibility().equals(Visibility.group_specific.name())){
                throw new IllegalArgumentException("Setting visibility as group_specific must be followed by giving groupId.");
            }
            return null;
        }
        if (!createSurveySectionDto.getVisibility().equals(Visibility.group_specific.name())){
            throw new IllegalArgumentException("Set section visibility to group_specific or remove groupId.");
        }

        Optional<RespondentGroup> optionalRespondentGroup = respondentGroupRepository.findById(UUID.fromString(groupId));
        if (optionalRespondentGroup.isEmpty()){
            throw new NoSuchElementException("Respondent group id not found: " + groupId);
        }

        SectionToUserGroup sectionToUserGroupEntity = new SectionToUserGroup();
        sectionToUserGroupEntity.setSection(surveySectionEntity);
        sectionToUserGroupEntity.setGroup(optionalRespondentGroup.get());

        return sectionToUserGroupEntity;
    }
}