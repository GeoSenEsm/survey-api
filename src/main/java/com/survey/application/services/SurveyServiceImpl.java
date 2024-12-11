package com.survey.application.services;

import com.survey.application.dtos.SurveySendingPolicyTimesDto;
import com.survey.application.dtos.surveyDtos.*;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.SurveyState;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequestScope
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final ModelMapper modelMapper;
    private final RespondentGroupRepository respondentGroupRepository;
    private final SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository;
    @PersistenceContext
    private final EntityManager entityManager;
    private final SurveyValidationService surveyValidationService;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final StorageService storageService;


    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, ModelMapper modelMapper,
                             RespondentGroupRepository respondentGroupRepository,
                             EntityManager entityManager,
                             SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository,
                             SurveyValidationService surveyValidationService,
                             ClaimsPrincipalService claimsPrincipalService, StorageService storageService) {
        this.surveyRepository = surveyRepository;
        this.modelMapper = modelMapper;
        this.respondentGroupRepository = respondentGroupRepository;
        this.entityManager = entityManager;
        this.surveyParticipationTimeSlotRepository = surveyParticipationTimeSlotRepository;
        this.surveyValidationService = surveyValidationService;
        this.claimsPrincipalService = claimsPrincipalService;
        this.storageService = storageService;
    }

    @Override
    public ResponseSurveyDto createSurvey(CreateSurveyDto createSurveyDto, List<MultipartFile> files) {
        surveyValidationService.validateImageChoiceFiles(createSurveyDto, files);
        Survey surveyEntity = mapToSurvey(createSurveyDto, files);
        surveyValidationService.validateShowSections(surveyEntity);

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


    @Override
    public List<ResponseSurveyShortSummariesDto> getSurveysShortSummaries() {
        OffsetDateTime endOfDay = OffsetDateTime.now(ZoneOffset.UTC).withHour(23).withMinute(59).withSecond(59);


        if (!claimsPrincipalService.isAnonymous() && claimsPrincipalService.findIdentityUser().getRole().equals("Respondent")) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);


            String jpql = "SELECT s FROM Survey s " + "JOIN s.policies p " + "JOIN p.timeSlots ts " + "WHERE ts.start <= :now AND ts.finish >= :now " + "AND NOT EXISTS (" + "   SELECT sp FROM SurveyParticipation sp " + "   WHERE sp.survey = s AND sp.identityUser.id = :identityUserId" + ")";

            TypedQuery<Survey> query = entityManager.createQuery(jpql, Survey.class);
            query.setParameter("now", now);
            query.setParameter("identityUserId", claimsPrincipalService.findIdentityUser().getId());

            return query.getResultStream().map(survey -> {
                List<SurveySendingPolicyTimesDto> timeSlotDtoList = survey.getPolicies().stream().flatMap(policy -> policy.getTimeSlots().stream()).filter(slot -> slot.getFinish().isBefore(endOfDay)).map(slot -> modelMapper.map(slot, SurveySendingPolicyTimesDto.class)).collect(Collectors.toList());

                ResponseSurveyShortSummariesDto dto = modelMapper.map(survey, ResponseSurveyShortSummariesDto.class);
                dto.setDates(timeSlotDtoList);
                return dto;
            }).collect(Collectors.toList());
        }

        return surveyRepository.findAll().stream()
                .map(survey -> {
                    List<SurveySendingPolicyTimesDto> timeSlotDtoList = survey.getPolicies().stream()
                            .flatMap(policy -> policy.getTimeSlots().stream())
                            .filter(slot -> slot.getFinish().isBefore(endOfDay))
                            .map(slot -> modelMapper.map(slot, SurveySendingPolicyTimesDto.class))
                            .collect(Collectors.toList());

                    ResponseSurveyShortSummariesDto dto = modelMapper.map(survey, ResponseSurveyShortSummariesDto.class);
                    dto.setDates(timeSlotDtoList);
                    return dto;
                })
                .filter(dto -> !dto.getDates().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public ResponseSurveyDto getSurveyById(UUID surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NoSuchElementException("Survey not found with id: " + surveyId));

        return modelMapper.map(survey, ResponseSurveyDto.class);
    }

    @Override
    public List<ResponseSurveyWithTimeSlotsDto> getAllSurveysWithTimeSlots(){
        List<Survey> surveys = entityManager.createQuery(
                "SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.policies p " +
                        "WHERE s.state = :state AND EXISTS (" +
                        "SELECT ts FROM SurveyParticipationTimeSlot ts " +
                        "WHERE ts.surveySendingPolicy = p " +
                        "AND ts.finish > CURRENT_TIMESTAMP " +
                        "AND ts.isDeleted = false)",
                Survey.class).setParameter("state", SurveyState.published).getResultList();

        TypedQuery<SurveyParticipationTimeSlot> timeSlotsQuery = entityManager.createQuery(
                "SELECT ts FROM SurveyParticipationTimeSlot ts " +
                        "WHERE ts.finish > CURRENT_TIMESTAMP " +
                        "AND NOT EXISTS (" +
                        "SELECT 1 FROM SurveyParticipation p " +
                        "WHERE p.date >= ts.start " +
                        "AND p.date <= ts.finish " +
                        "AND p.identityUser.id = :identityUserId" +
                        ")",
                SurveyParticipationTimeSlot.class
        );
        timeSlotsQuery.setParameter("identityUserId", claimsPrincipalService.findIdentityUser().getId());
        List<SurveyParticipationTimeSlot> timeSlots = timeSlotsQuery.getResultList();

        return surveys.stream()
                .map(survey -> {
                    ResponseSurveyDto surveyDto = modelMapper.map(survey, ResponseSurveyDto.class);

                    List<SurveySendingPolicyTimesDto> timeSlotDtoList = timeSlots.stream()
                            .filter(slot -> survey.getPolicies().contains(slot.getSurveySendingPolicy()))
                            .map(slot -> modelMapper.map(slot, SurveySendingPolicyTimesDto.class))
                            .collect(Collectors.toList());

                    ResponseSurveyWithTimeSlotsDto responseDto = new ResponseSurveyWithTimeSlotsDto();
                    responseDto.setSurvey(surveyDto);
                    responseDto.setSurveySendingPolicyTimes(timeSlotDtoList);

                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void publishSurvey(UUID surveyId) {
        Survey survey = findSurveyById(surveyId);

        if (isSurveyPublished(surveyId)) {
            throw new IllegalStateException("Survey is already published.");
        }
        survey.setState(SurveyState.published);
        surveyRepository.saveAndFlush(survey);
    }

    @Override
    public void deleteSurvey(UUID surveyId) {
        if (isSurveyPublished(surveyId)){
            throw new IllegalStateException("Cannot delete published survey.");
        }

        storageService.deleteSurveyImages(getSurveyNameById(surveyId));
        surveyRepository.delete(findSurveyById(surveyId));
    }

    private String getSurveyNameById(UUID surveyId){
        return surveyRepository.findSurveyNameBySurveyId(surveyId);
    }

    private boolean isSurveyPublished(UUID surveyId){
        return findSurveyById(surveyId).getState() == SurveyState.published;
    }

    private Survey findSurveyById(UUID surveyId){
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NoSuchElementException("Survey not found with id: " + surveyId));
    }

    private Survey mapToSurvey(CreateSurveyDto createSurveyDto, List<MultipartFile> files){
        Survey survey = new Survey();
        survey.setName(createSurveyDto.getName());
        survey.setState(SurveyState.created);
        survey.setSections(createSurveyDto.getSections().stream()
                .map(sectionDto -> mapToSurveySection(sectionDto, survey, files))
                .collect(Collectors.toList()));
        return survey;
    }

    private SurveySection mapToSurveySection(CreateSurveySectionDto sectionDto, Survey surveyEntity, List<MultipartFile> files){
        SurveySection surveySection = modelMapper.map(sectionDto, SurveySection.class);
        surveySection.setId(null);
        surveySection.setSurvey(surveyEntity);

        SectionToUserGroup sectionToUserGroup = getSectionToUserGroup(sectionDto, surveySection);
        surveySection.setSectionToUserGroups(sectionToUserGroup != null ? List.of(sectionToUserGroup) : null);
        surveySection.setQuestions(sectionDto.getQuestions().stream()
                .map(questionDto -> mapToQuestion(questionDto, surveySection, surveyEntity.getName(), files))
                .collect(Collectors.toList())
        );

        return surveySection;
    }

    private Question mapToQuestion(CreateQuestionDto questionDto, SurveySection surveySection, String surveyId, List<MultipartFile> files){
        Question question = modelMapper.map(questionDto, Question.class);
        question.setSection(surveySection);

        if (question.getQuestionType().equals(QuestionType.single_choice) || question.getQuestionType().equals(QuestionType.multiple_choice)){
            if (questionDto.getOptions() == null){
                throw new IllegalArgumentException("Question type set as " + question.getQuestionType().name() + " - must include a list of options in dto.");
            }
            question.setNumberRange(null);
            question.setOptions(questionDto.getOptions().stream()
                    .map(optionDto -> mapToOption(optionDto, question))
                    .collect(Collectors.toList()));
        }

        if (question.getQuestionType().equals(QuestionType.linear_scale)){
            if (questionDto.getNumberRange() == null){
                throw new IllegalArgumentException("Question type set as linear_scale - must include number range in dto.");
            }
            question.setNumberRange(mapToNumberRange(questionDto.getNumberRange(), question));
            question.setOptions(null);
        }

        if (question.getQuestionType().equals(QuestionType.yes_no_choice) || question.getQuestionType().equals(QuestionType.number_input)) {
            question.setNumberRange(null);
            question.setOptions(null);
        }

        if (question.getQuestionType().equals(QuestionType.image_choice)) {
            if (questionDto.getOptions() == null){
                throw new IllegalArgumentException("Question type set as image_choice - must include a list of options in dto.");
            }
            question.setNumberRange(null);
            question.setOptions(mapOptionsWithFiles(questionDto.getOptions(), question, surveySection.getOrder().toString(), surveyId, files));
        }

        return question;
    }

    private NumberRange mapToNumberRange(CreateNumberRangeOptionDto numberRangeOptionDto, Question question){
        NumberRange numberRange = modelMapper.map(numberRangeOptionDto, NumberRange.class);
        numberRange.setQuestion(question);
        return numberRange;
    }

    private Option mapToOption(CreateOptionDto optionDto, Question question) {
        Option option = modelMapper.map(optionDto, Option.class);
        option.setQuestion(question);
        return option;
    }

    private List<Option> mapOptionsWithFiles(List<CreateOptionDto> optionDtoList, Question question, String surveySectionOrder, String surveyName, List<MultipartFile> files) {
        return optionDtoList.stream()
                .map(optionDto -> {
                    Option option = mapToOption(optionDto, question);
                    int optionIndex = optionDto.getOrder() - 1;
                    if (optionIndex < files.size()) {
                        MultipartFile file = files.get(optionIndex);
                        String imagePath;
                        try {
                            imagePath = storageService.store(file, surveyName, surveySectionOrder, question.getOrder().toString(), option.getOrder().toString());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to store file for option: " + option.getLabel(), e);
                        }
                        option.setImagePath(imagePath);
                    }
                    return option;
                })
                .collect(Collectors.toList());
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