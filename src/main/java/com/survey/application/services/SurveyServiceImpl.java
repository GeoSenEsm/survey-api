package com.survey.application.services;

import com.survey.api.security.Role;
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
import java.util.*;
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
    private final SurveySendingPolicyRepository surveySendingPolicyRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;


    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, ModelMapper modelMapper,
                             RespondentGroupRepository respondentGroupRepository,
                             EntityManager entityManager,
                             SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository,
                             SurveyValidationService surveyValidationService,
                             ClaimsPrincipalService claimsPrincipalService, StorageService storageService, SurveySendingPolicyRepository surveySendingPolicyRepository, SurveyParticipationRepository surveyParticipationRepository) {
        this.surveyRepository = surveyRepository;
        this.modelMapper = modelMapper;
        this.respondentGroupRepository = respondentGroupRepository;
        this.entityManager = entityManager;
        this.surveyParticipationTimeSlotRepository = surveyParticipationTimeSlotRepository;
        this.surveyValidationService = surveyValidationService;
        this.claimsPrincipalService = claimsPrincipalService;
        this.storageService = storageService;
        this.surveySendingPolicyRepository = surveySendingPolicyRepository;
        this.surveyParticipationRepository = surveyParticipationRepository;
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
                .sorted(Comparator.comparing(Survey::getCreationDate).reversed())
                .toList();

        return surveys.stream()
                .map(survey -> modelMapper.map(survey, ResponseSurveyDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseSurveyShortDto> getSurveysShort() {
        return surveyRepository.findAll().stream()
                .sorted(Comparator.comparing(Survey::getCreationDate).reversed())
                .map(survey -> modelMapper.map(survey, ResponseSurveyShortDto.class))
                .collect(Collectors.toList());
    }


    @Override
    public List<ResponseSurveyShortSummariesDto> getSurveysShortSummaries() {
        OffsetDateTime endOfDay = OffsetDateTime.now(ZoneOffset.UTC).withHour(23).withMinute(59).withSecond(59);


        if (!claimsPrincipalService.isAnonymous() && claimsPrincipalService.findIdentityUser().getRole().equalsIgnoreCase(Role.RESPONDENT.getRoleName())) {
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
                .sorted(Comparator.comparing(Survey::getCreationDate).reversed())
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

//    TODO make this more efficient
    @Override
    public List<ResponseSurveyWithTimeSlotsDto> getAllSurveysWithTimeSlots() {
        List<Survey> surveys = entityManager.createQuery(
                        "SELECT DISTINCT s FROM Survey s WHERE s.state = :state ORDER BY s.creationDate DESC",
                        Survey.class)
                .setParameter("state", SurveyState.published)
                .getResultList();

        UUID identityUserId = claimsPrincipalService.findIdentityUser().getId();

        return surveys.stream()
                .map(survey -> {
                    ResponseSurveyDto surveyDto = modelMapper.map(survey, ResponseSurveyDto.class);

                    List<SurveySendingPolicyTimesDto> validTimeSlots = survey.getPolicies().stream()
                            .flatMap(policy -> policy.getTimeSlots().stream())
                            .filter(slot -> isValidTimeSlot(slot, identityUserId, survey.getId()))
                            .map(slot -> modelMapper.map(slot, SurveySendingPolicyTimesDto.class))
                            .collect(Collectors.toList());

                    if (!validTimeSlots.isEmpty()) {
                        ResponseSurveyWithTimeSlotsDto responseDto = new ResponseSurveyWithTimeSlotsDto();
                        responseDto.setSurvey(surveyDto);
                        responseDto.setSurveySendingPolicyTimes(validTimeSlots);
                        return responseDto;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
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

    @Override
    @Transactional
    public ResponseSurveyDto updateSurvey(UUID surveyId, CreateSurveyDto createSurveyDto, List<MultipartFile> files) {
        if (isSurveyPublished(surveyId)){
            throw new IllegalStateException("Cannot update published survey.");
        }
        List<SurveySendingPolicy> policies = surveySendingPolicyRepository.findAllBySurveyId(surveyId);
        OffsetDateTime originalSurveyCreationDateTime = findSurveyById(surveyId).getCreationDate();

        deleteSurvey(surveyId);

        surveyValidationService.validateImageChoiceFiles(createSurveyDto, files);
        Survey surveyEntity = mapToSurvey(createSurveyDto, files);
        surveyEntity.setId(surveyId);
        surveyEntity.setCreationDate(originalSurveyCreationDateTime);
        surveyValidationService.validateShowSections(surveyEntity);

        Survey dbSurvey = surveyRepository.saveAndFlush(surveyEntity);
        entityManager.refresh(dbSurvey);

        if (!policies.isEmpty()) {
            surveySendingPolicyRepository.saveAllAndFlush(policies);
        }

        return modelMapper.map(dbSurvey, ResponseSurveyDto.class);
    }

    @Override
    public boolean doesNewerDataExistsInDB(Long maxRowVersionFromMobileApp) {
        String sql = """
                SELECT MAX(row_version) AS maxRowVersion FROM (
                    SELECT MAX(CAST(s.row_version AS bigint)) AS row_version FROM survey s
                    UNION ALL
                    SELECT MAX(CAST(sec.row_version AS bigint)) AS row_version FROM survey_section sec
                    UNION ALL
                    SELECT MAX(CAST(q.row_version AS bigint)) AS row_version FROM question q
                    UNION ALL
                    SELECT MAX(CAST(o.row_version AS bigint)) AS row_version FROM [option] o
                    UNION ALL
                    SELECT MAX(CAST(nr.row_version AS bigint)) AS row_version FROM number_range nr
                    UNION ALL
                    SELECT MAX(CAST(stg.row_version AS bigint)) AS row_version FROM section_to_user_group stg
                    UNION ALL
                    SELECT MAX(CAST(ssp.row_version AS bigint)) FROM survey_sending_policy ssp
                    UNION ALL
                    SELECT MAX(CAST(ts.row_version AS bigint)) FROM survey_participation_time_slot ts
                    UNION ALL
                    SELECT MAX(CAST(sp.row_version AS bigint)) FROM survey_participation sp WHERE sp.respondent_id = :identityUserId
                 ) subquery
            """;

        UUID identityUserId = claimsPrincipalService.findIdentityUser().getId();
        Long maxRowVersionFromDB = (Long) entityManager
                .createNativeQuery(sql)
                .setParameter("identityUserId", identityUserId)
                .getSingleResult();

        return maxRowVersionFromDB > maxRowVersionFromMobileApp;
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

    private boolean isValidTimeSlot(SurveyParticipationTimeSlot slot, UUID identityUserId, UUID surveyId) {
        if (slot.isDeleted() || slot.getFinish().isBefore(OffsetDateTime.now())) {
            return false;
        }

        return !surveyParticipationRepository.existsBySurveyIdAndIdentityUserIdAndDateBetween(surveyId, identityUserId, slot.getStart(), slot.getFinish());
    }

    private Survey mapToSurvey(CreateSurveyDto createSurveyDto, List<MultipartFile> files){
        Survey survey = new Survey();
        survey.setName(createSurveyDto.getName());
        survey.setState(SurveyState.created);
        survey.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
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

        if (question.getQuestionType().equals(QuestionType.image_choice)) {
            if (questionDto.getOptions() == null){
                throw new IllegalArgumentException("Question type set as image_choice - must include a list of options in dto.");
            }
            question.setNumberRange(null);
            question.setOptions(mapOptionsWithFiles(questionDto.getOptions(), question, surveySection.getOrder().toString(), surveyId, files));
        }

        if (question.getQuestionType().equals(QuestionType.yes_no_choice) ||
                question.getQuestionType().equals(QuestionType.number_input) ||
                question.getQuestionType().equals(QuestionType.text_input)){

            question.setNumberRange(null);
            question.setOptions(null);
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