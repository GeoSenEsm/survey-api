package com.survey.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.security.Role;
import com.survey.api.validation.SendSurveyResponseDtoValidator;
import com.survey.application.dtos.*;
import com.survey.application.dtos.surveyDtos.*;
import com.survey.application.events.SurveyResponseSubmittedEvent;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.repository.*;
import com.survey.infrastructure.mongo.documents.SurveyResponseDocument;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequestScope
public class SurveyResponsesServiceImpl implements SurveyResponsesService {
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final SurveyRepository surveyRepository;
    private final OptionRepository optionRepository;
    private final QuestionRepository questionRepository;
    private final ClaimsPrincipalServiceImpl claimsPrincipalServiceImpl;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;
    private final SendSurveyResponseDtoValidator sendSurveyResponseDtoValidator;
    private final SurveyParticipationTimeValidationService surveyParticipationTimeValidationService;
    private final SensorDataRepository sensorDataRepository;
    private final SensorParameterDefinitionRepository sensorParameterDefinitionRepository;
    private final SensorTypeRepository sensorTypeRepository;
    private final IdentityUserRepository identityUserRepository;
    private final LocalizationDataRepository localizationDataRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RespondentTimeZoneService respondentTimeZoneService;


    @Autowired
    public SurveyResponsesServiceImpl(
            SurveyParticipationRepository surveyParticipationRepository,
            SurveyRepository surveyRepository,
            OptionRepository optionRepository,
            QuestionRepository questionRepository,
            ClaimsPrincipalServiceImpl claimsPrincipalServiceImpl,
            ModelMapper modelMapper,
            ObjectMapper objectMapper,
            EntityManager entityManager,
            SendSurveyResponseDtoValidator sendSurveyResponseDtoValidator,
            SurveyParticipationTimeValidationService surveyParticipationTimeValidationService,
            SensorDataRepository sensorDataRepository,
            SensorParameterDefinitionRepository sensorParameterDefinitionRepository,
            SensorTypeRepository sensorTypeRepository,
            IdentityUserRepository identityUserRepository,
            LocalizationDataRepository localizationDataRepository,
            ApplicationEventPublisher eventPublisher,
            RespondentTimeZoneService respondentTimeZoneService) {
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.surveyRepository = surveyRepository;
        this.optionRepository = optionRepository;
        this.questionRepository = questionRepository;
        this.claimsPrincipalServiceImpl = claimsPrincipalServiceImpl;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.sendSurveyResponseDtoValidator = sendSurveyResponseDtoValidator;
        this.surveyParticipationTimeValidationService = surveyParticipationTimeValidationService;
        this.sensorDataRepository = sensorDataRepository;
        this.sensorParameterDefinitionRepository = sensorParameterDefinitionRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.identityUserRepository = identityUserRepository;
        this.localizationDataRepository = localizationDataRepository;
        this.eventPublisher = eventPublisher;
        this.respondentTimeZoneService = respondentTimeZoneService;
    }

    Survey findSurveyById(UUID surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid survey ID - survey doesn't exist"));
    }

    private List<Question> findQuestionsByIds(List<UUID> questionIds, UUID surveyId) {
        return questionRepository.findAllByIds(surveyId, questionIds);
    }

    private SurveyParticipation saveSurveyParticipationOnline(IdentityUser identityUser, Survey survey, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate){
        OffsetDateTime surveyParticipationDateToSave = surveyParticipationTimeValidationService
                .getCorrectSurveyParticipationDateTimeOnline(identityUser.getId(), survey.getId(), surveyStartDate, surveyFinishDate);

        return buildParticipation(identityUser, survey, surveyParticipationDateToSave);
    }

    private SurveyParticipation saveSurveyParticipationOffline(IdentityUser identityUser, Survey survey, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate){
        OffsetDateTime surveyParticipationDateToSave = surveyParticipationTimeValidationService
                .getCorrectSurveyParticipationDateTimeOffline(identityUser.getId(), survey.getId(), surveyStartDate, surveyFinishDate);

        if (surveyParticipationDateToSave == null){
            return null;
        }

        return buildParticipation(identityUser, survey, surveyParticipationDateToSave);
    }

    private SurveyParticipation buildParticipation(IdentityUser identityUser, Survey survey, OffsetDateTime participationUtc) {
        OffsetDateTime utc = respondentTimeZoneService.toUtc(participationUtc);
        var localParts = respondentTimeZoneService.toLocalParts(
                utc, respondentTimeZoneService.resolveZoneId(identityUser));

        SurveyParticipation participation = new SurveyParticipation();
        participation.setIdentityUser(identityUser);
        participation.setDate(utc);
        participation.setLocalDate(localParts.date());
        participation.setLocalTime(localParts.time());
        participation.setSurvey(survey);
        return participation;
    }

    private Map<UUID, Option> findOptionsBySurveyId(List<UUID> questionIds) {
        return optionRepository.findByQuestionIdIn(questionIds)
                .stream()
                .collect(Collectors.toMap(Option::getId, option -> option));
    }

    private SurveyParticipation mapQuestionAnswers(SendSurveyResponseDto sendSurveyResponseDto, SurveyParticipation surveyParticipation, Survey survey) {
        List<UUID> questionIds = sendSurveyResponseDto.getAnswers().stream()
                .map(AnswerDto::getQuestionId)
                .collect(Collectors.toList());

        List<Question> questions = findQuestionsByIds(questionIds, survey.getId());

        Map<UUID, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, question -> question));

        Map<UUID, Option> optionsMap = findOptionsBySurveyId(questionIds);

        List<QuestionAnswer> questionAnswers = sendSurveyResponseDto.getAnswers().stream()
                .map(answerDto -> {
                    Question question = questionMap.get(answerDto.getQuestionId());
                    if (question == null) {
                        throw new IllegalArgumentException("Invalid question ID: " + answerDto.getQuestionId());
                    }
                    QuestionAnswer questionAnswer = new QuestionAnswer();
                    questionAnswer.setSurveyParticipation(surveyParticipation);
                    questionAnswer.setQuestion(question);

                    if (question.getQuestionType().equals(QuestionType.single_choice) || question.getQuestionType().equals(QuestionType.multiple_choice) || question.getQuestionType().equals(QuestionType.image_choice)) {
                        List<OptionSelection> optionSelections = answerDto.getSelectedOptions().stream()
                                .map(selectedOptionDto -> {
                                    Option option = optionsMap.get(selectedOptionDto.getOptionId());
                                    OptionSelection optionSelection = new OptionSelection();
                                    optionSelection.setQuestionAnswer(questionAnswer);
                                    optionSelection.setOption(option);
                                    return optionSelection;
                                }).collect(Collectors.toList());
                        questionAnswer.setOptionSelections(optionSelections);
                    }

                    if (question.getQuestionType().equals(QuestionType.yes_no_choice)) {
                        questionAnswer.setYesNoAnswer(answerDto.getYesNoAnswer());
                    }

                    if (question.getQuestionType().equals(QuestionType.number_input) || question.getQuestionType().equals(QuestionType.linear_scale)) {
                        questionAnswer.setNumericAnswer(answerDto.getNumericAnswer());
                    }

                    if (question.getQuestionType().equals(QuestionType.text_input)){
                        TextAnswer textAnswer = new TextAnswer();
                        textAnswer.setTextAnswerContent(answerDto.getTextAnswer());
                        textAnswer.setQuestionAnswer(questionAnswer);
                        questionAnswer.setTextAnswer(textAnswer);
                    }

                    return questionAnswer;
                }).collect(Collectors.toList());

        surveyParticipation.setQuestionAnswers(questionAnswers);
        return surveyParticipation;
    }

    private SurveyParticipationDto mapToDto(SurveyParticipation surveyParticipation, SendSurveyResponseDto sendSurveyResponseDto, IdentityUser identityUser) {
        SurveyParticipation finalSurveyParticipation = surveyParticipationRepository.saveAndFlush(surveyParticipation);
        entityManager.refresh(finalSurveyParticipation);
        SurveyParticipationDto surveyParticipationDto = modelMapper.map(finalSurveyParticipation, SurveyParticipationDto.class);
        surveyParticipationDto.setSurveyId(sendSurveyResponseDto.getSurveyId());
        surveyParticipationDto.setRespondentId(identityUser.getId());
        surveyParticipationDto.setSurveyStartDate(sendSurveyResponseDto.getStartDate());
        surveyParticipationDto.setSurveyFinishDate(sendSurveyResponseDto.getFinishDate());
        return surveyParticipationDto;
    }

    @Override
    @Transactional
    public SurveyParticipationDto saveSurveyResponseOnline(SendOnlineSurveyResponseDto sendOnlineSurveyResponseDto) {
        IdentityUser identityUser = claimsPrincipalServiceImpl.findIdentityUser();
        Survey survey = findSurveyById(sendOnlineSurveyResponseDto.getSurveyId());

        SurveyParticipation surveyParticipation = saveSurveyParticipationOnline(identityUser, survey, sendOnlineSurveyResponseDto.getStartDate(), sendOnlineSurveyResponseDto.getFinishDate());
        SurveyParticipation finalSurveyParticipation = mapQuestionAnswers(sendOnlineSurveyResponseDto, surveyParticipation, survey);
        surveyParticipationRepository.save(finalSurveyParticipation);
        saveSensorData(sendOnlineSurveyResponseDto, finalSurveyParticipation, identityUser);
        SurveyParticipationDto dto = mapToDto(finalSurveyParticipation, sendOnlineSurveyResponseDto, identityUser);
        publishResponseSubmittedEvent(finalSurveyParticipation, identityUser, survey, sendOnlineSurveyResponseDto);
        return dto;
    }

    @Override
    @Transactional
    public List<SurveyParticipationDto> saveSurveyResponsesOffline(List<SendOfflineSurveyResponseDto> sendOfflineSurveyResponseDtoList) {
        IdentityUser identityUser = claimsPrincipalServiceImpl.findIdentityUser();

        return sendOfflineSurveyResponseDtoList.stream()
                .filter(dto -> sendSurveyResponseDtoValidator.isValid(dto, null))
                .map(dto -> {
                    Survey survey = findSurveyById(dto.getSurveyId());
                    SurveyParticipation participation = saveSurveyParticipationOffline(identityUser, survey, dto.getStartDate(), dto.getFinishDate());
                    if (participation == null) return null;

                    SurveyParticipation finalParticipation = mapQuestionAnswers(dto, participation, survey);
                    surveyParticipationRepository.save(finalParticipation);
                    saveSensorData(dto, finalParticipation, identityUser);
                    SurveyParticipationDto mapped = mapToDto(finalParticipation, dto, identityUser);
                    publishResponseSubmittedEvent(finalParticipation, identityUser, survey, dto);
                    return mapped;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private void publishResponseSubmittedEvent(
            SurveyParticipation participation,
            IdentityUser identityUser,
            Survey survey,
            SendSurveyResponseDto submittedDto
    ) {
        SurveyResponseDocument document = buildResponseDocument(participation, identityUser, survey, submittedDto);
        eventPublisher.publishEvent(new SurveyResponseSubmittedEvent(document));
    }

    private SurveyResponseDocument buildResponseDocument(
            SurveyParticipation participation,
            IdentityUser identityUser,
            Survey survey,
            SendSurveyResponseDto submittedDto
    ) {
        List<SurveyResponseDocument.Answer> answers = participation.getQuestionAnswers() == null
                ? List.of()
                : participation.getQuestionAnswers().stream()
                        .map(this::toDocumentAnswer)
                        .toList();

        SurveyResponseDocument.SensorReading sensorReading = null;
        if (submittedDto.getSensorData() != null) {
            SensorDataDto sensor = submittedDto.getSensorData();
            sensorReading = SurveyResponseDocument.SensorReading.builder()
                    .dateTime(sensor.getDateTime())
                    .source(sensor.getSource())
                    .values(sensor.getValues().stream()
                            .map(value -> SurveyResponseDocument.SensorValue.builder()
                                    .parameterCode(value.getParameterCode())
                                    .value(value.getValue())
                                    .build())
                            .toList())
                    .build();
        }

        return SurveyResponseDocument.builder()
                .participationId(participation.getId())
                .surveyId(survey.getId())
                .surveyName(survey.getName())
                .respondentId(identityUser.getId())
                .respondentUsername(identityUser.getUsername())
                .participationDate(participation.getDate())
                .localDate(participation.getLocalDate())
                .localTime(participation.getLocalTime())
                .surveyStartDate(submittedDto.getStartDate())
                .surveyFinishDate(submittedDto.getFinishDate())
                .answers(answers)
                .sensorData(sensorReading)
                .persistedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    private SurveyResponseDocument.Answer toDocumentAnswer(QuestionAnswer questionAnswer) {
        Question question = questionAnswer.getQuestion();
        // Leave a field null when the question type doesn't use it: the doc
        // marks these @Field(write = NON_NULL) so nulls are omitted from
        // both the Mongo document and the JSON output.
        List<SurveyResponseDocument.SelectedOption> selectedOptions = null;
        if (questionAnswer.getOptionSelections() != null && !questionAnswer.getOptionSelections().isEmpty()) {
            selectedOptions = questionAnswer.getOptionSelections().stream()
                    .map(sel -> SurveyResponseDocument.SelectedOption.builder()
                            .optionId(sel.getOption().getId())
                            .label(sel.getOption().getLabel())
                            .build())
                    .toList();
        }
        String textAnswerContent = questionAnswer.getTextAnswer() != null
                ? questionAnswer.getTextAnswer().getTextAnswerContent()
                : null;
        return SurveyResponseDocument.Answer.builder()
                .questionId(question.getId())
                .questionContent(question.getContent())
                .questionType(question.getQuestionType() != null
                        ? question.getQuestionType().name()
                        : null)
                .selectedOptions(selectedOptions)
                .numericAnswer(questionAnswer.getNumericAnswer())
                .yesNoAnswer(questionAnswer.getYesNoAnswer())
                .textAnswer(textAnswerContent)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyResultDto> getSurveyResults(UUID surveyId, UUID identityUserId, OffsetDateTime dateFrom, OffsetDateTime dateTo, Boolean outsideResearchArea) {
        List<SurveyParticipation> participationList = surveyParticipationRepository
                .findByFiltersWithFetch(surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea);

        return participationList.stream()
                .flatMap(this::mapParticipationToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyResultDto> getSurveyResultsBatch(UUID surveyId, UUID identityUserId, OffsetDateTime dateFrom, OffsetDateTime dateTo, Boolean outsideResearchArea, int offset, int limit) {
        List<SurveyParticipation> participationList = surveyParticipationRepository
                .findByFiltersWithFetchBatch(surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea, offset, limit);

        return participationList.stream()
                .flatMap(this::mapParticipationToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public void streamSurveyResults(OutputStream outputStream, UUID surveyId, UUID identityUserId, OffsetDateTime dateFrom, OffsetDateTime dateTo, Boolean outsideResearchArea) throws Exception {
        // Start JSON array
        outputStream.write("[".getBytes());
        outputStream.flush();

        // TRUE STREAMING: Fetch and write in batches, not all at once
        int batchSize = 1000;
        int offset = 0;
        boolean first = true;

        while (true) {
            // Fetch one batch at a time
            List<SurveyResultDto> batch = getSurveyResultsBatch(surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea, offset, batchSize);

            if (batch.isEmpty()) {
                break; // No more data
            }

            // Write this batch to stream immediately
            for (SurveyResultDto dto : batch) {
                if (!first) {
                    outputStream.write(",".getBytes());
                }
                first = false;

                String json = objectMapper.writeValueAsString(dto);
                outputStream.write(json.getBytes());
            }

            // Flush after each batch to keep connection alive
            outputStream.flush();

            if (batch.size() < batchSize) {
                break; // Last batch
            }

            offset += batchSize;

            // Safety limit
            if (offset > 100000) {
                break;
            }
        }

        // Close JSON array
        outputStream.write("]".getBytes());
        outputStream.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public void streamAllSurveyResults(OutputStream outputStream) throws Exception {
        // Start JSON array
        outputStream.write("[".getBytes());
        outputStream.flush();

        List<IdentityUser> identityUserList = identityUserRepository.findByRole(Role.RESPONDENT.getRoleName());

        if (identityUserList.isEmpty()) {
            // Close empty JSON array
            outputStream.write("]".getBytes());
            outputStream.flush();
            return;
        }

        // Batch users to prevent "query too long" error with IN clause
        // 500 UUIDs ≈ 20,000 chars (safe limit, leaving room for SQL)
        int batchSize = 500;
        boolean first = true;

        for (int i = 0; i < identityUserList.size(); i += batchSize) {
            List<IdentityUser> batchUsers = identityUserList.subList(i, Math.min(i + batchSize, identityUserList.size()));
            List<UUID> batchUserIds = batchUsers.stream()
                    .map(IdentityUser::getId)
                    .toList();

            // Fetch all data for this batch with optimized queries using fetch joins
            List<LocalizationData> localizationDataList = localizationDataRepository.findAllByIdentityUserIdsWithFetch(batchUserIds);
            List<SensorData> sensorDataList = sensorDataRepository.findAllByRespondentIdsWithFetch(batchUserIds);
            List<SurveyParticipation> surveyParticipationList = surveyParticipationRepository.findAllByIdentityUserIdsWithFetch(batchUserIds);

            // Group data by user ID for efficient lookup
            Map<UUID, List<LocalizationData>> localizationByUser = localizationDataList.stream()
                    .collect(Collectors.groupingBy(ld -> ld.getIdentityUser().getId()));
            Map<UUID, List<SensorData>> sensorByUser = sensorDataList.stream()
                    .collect(Collectors.groupingBy(sd -> sd.getRespondent().getId()));
            Map<UUID, List<SurveyParticipation>> participationByUser = surveyParticipationList.stream()
                    .collect(Collectors.groupingBy(sp -> sp.getIdentityUser().getId()));

            // Stream each user's data immediately
            for (IdentityUser user : batchUsers) {
                UUID userId = user.getId();
                AllResultsDto dto = mapIdentityUserToDto(
                        user,
                        localizationByUser.getOrDefault(userId, List.of()),
                        sensorByUser.getOrDefault(userId, List.of()),
                        participationByUser.getOrDefault(userId, List.of())
                );

                if (!first) {
                    outputStream.write(",".getBytes());
                }
                first = false;

                String json = objectMapper.writeValueAsString(dto);
                outputStream.write(json.getBytes());
            }

            // Flush after each batch to keep connection alive
            outputStream.flush();
        }

        // Close JSON array
        outputStream.write("]".getBytes());
        outputStream.flush();
    }

    private AllResultsDto mapIdentityUserToDto(IdentityUser identityUser, List<LocalizationData> localizationDataList, List<SensorData> sensorDataList, List<SurveyParticipation> surveyParticipationList) {
        AllResultsDto allResultsDto = new AllResultsDto();
        allResultsDto.setRespondentId(identityUser.getId());
        allResultsDto.setUsername(identityUser.getUsername());
        allResultsDto.setLocalizationDataList(mapLocalizationDataToDto(localizationDataList));
        allResultsDto.setSensorDataList(mapSensorDataToDto(sensorDataList));
        allResultsDto.setSurveyResults(mapSurveyParticipationToDto(surveyParticipationList));
        return allResultsDto;
    }
    private List<AllResultsLocalizationDataDto> mapLocalizationDataToDto(List<LocalizationData> localizationDataList) {
        return localizationDataList.stream()
                .map(ld -> new AllResultsLocalizationDataDto(
                        ld.getId(),
                        ld.getLatitude(),
                        ld.getLongitude(),
                        ld.getDateTime(),
                        ld.getOutsideResearchArea(),
                        ld.getSurveyParticipation() != null ? ld.getSurveyParticipation().getId() : null,
                        ld.getAccuracyMeters()
                ))
                .collect(Collectors.toList());
    }

    private List<AllResultsSensorDataDto> mapSensorDataToDto(List<SensorData> sensorDataList) {
        return sensorDataList.stream()
                .map(sd -> new AllResultsSensorDataDto(
                        sd.getId(),
                        sd.getDateTime(),
                        sd.getSource(),
                        sd.getValues().stream()
                                .map(value -> new SensorDataValueDto(
                                        value.getParameterDefinition().getCode(),
                                        value.getValue()))
                                .toList(),
                        sd.getSurveyParticipation() != null ? sd.getSurveyParticipation().getId() : null
                ))
                .collect(Collectors.toList());
    }

    private List<AllResultsSurveyParticipationDto> mapSurveyParticipationToDto(List<SurveyParticipation> surveyParticipationList) {
        return surveyParticipationList.stream()
                .map(sp -> new AllResultsSurveyParticipationDto(
                        sp.getId(),
                        sp.getSurvey().getId(),
                        sp.getSurvey().getName(),
                        sp.getDate(),
                        sp.getQuestionAnswers().stream()
                                .map(qa -> new AllResultsQuestionAnswerDto(
                                        qa.getQuestion().getContent(),
                                        extractAnswers(qa)
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
    private void saveSensorData(SendSurveyResponseDto sendSurveyResponseDto, SurveyParticipation surveyParticipation, IdentityUser identityUser) {
        if (sendSurveyResponseDto.getSensorData() != null) {
            SensorData sensorData = toSensorDataEntity(sendSurveyResponseDto.getSensorData(), identityUser);
            sensorData.setSurveyParticipation(surveyParticipation);
            sensorDataRepository.save(sensorData);
        }
    }

    private Stream<SurveyResultDto> mapParticipationToDto(SurveyParticipation surveyParticipation) {
        return surveyParticipation.getQuestionAnswers().stream()
                .map(questionAnswer -> createSurveyResultDto(surveyParticipation, questionAnswer));
    }

    private SurveyResultDto createSurveyResultDto(SurveyParticipation surveyParticipation, QuestionAnswer questionAnswer) {
        SurveyResultDto dto = new SurveyResultDto();
        dto.setSurveyName(surveyParticipation.getSurvey().getName());
        dto.setQuestion(questionAnswer.getQuestion().getContent());
        dto.setResponseDate(surveyParticipation.getDate());
        dto.setRespondentId(surveyParticipation.getIdentityUser().getId());
        dto.setAnswers(extractAnswers(questionAnswer));
        dto.setLocalizationData(extractLocalizationData(surveyParticipation));
        dto.setSensorData(extractSensorData(surveyParticipation));
        return dto;
    }

    private List<Object> extractAnswers(QuestionAnswer questionAnswer) {
        List<Object> answers = new ArrayList<>();

        Optional.ofNullable(questionAnswer.getNumericAnswer())
                .ifPresent(answers::add);

        if (questionAnswer.getOptionSelections() != null) {
            answers.addAll(questionAnswer.getOptionSelections().stream()
                    .map(optionSelection -> optionSelection.getOption().getLabel())
                    .toList());
        }

        Optional.ofNullable(questionAnswer.getYesNoAnswer())
                .ifPresent(answers::add);

        Optional.ofNullable(questionAnswer.getTextAnswer())
                .map(TextAnswer::getTextAnswerContent)
                .ifPresent(answers::add);

        return answers;
    }
    private SensorDataDto extractSensorData(SurveyParticipation sp) {
        if(sp.getSensorData() != null){
            SensorData sensorData = sp.getSensorData();
            return new SensorDataDto(
                    sensorData.getDateTime(),
                    sensorData.getSource(),
                    sensorData.getValues().stream()
                            .map(value -> new SensorDataValueDto(
                                    value.getParameterDefinition().getCode(),
                                    value.getValue()))
                            .toList());
        }
        return null;
    }

    private SensorData toSensorDataEntity(SensorDataDto dto, IdentityUser identityUser) {
        SensorType sourceSensorType = sensorTypeRepository.findByCode(dto.getSource())
                .orElseThrow(() -> new IllegalArgumentException("Unknown sensor source: " + dto.getSource()));
        Map<String, SensorParameterDefinition> parametersByCode = sensorParameterDefinitionRepository.findAll().stream()
                .collect(Collectors.toMap(SensorParameterDefinition::getCode, parameter -> parameter));

        SensorData sensorData = new SensorData();
        sensorData.setRespondent(identityUser);
        sensorData.setDateTime(dto.getDateTime());
        sensorData.setSource(dto.getSource());
        sensorData.setSourceSensorType(sourceSensorType);
        dto.getValues().forEach(valueDto -> {
            SensorParameterDefinition parameterDefinition = parametersByCode.get(valueDto.getParameterCode());
            if (parameterDefinition == null || !parameterDefinition.isActive()) {
                throw new IllegalArgumentException("Unknown or inactive sensor parameter: " + valueDto.getParameterCode());
            }
            SensorDataParameterValue value = new SensorDataParameterValue();
            value.setSensorData(sensorData);
            value.setParameterDefinition(parameterDefinition);
            value.setValue(valueDto.getValue());
            sensorData.getValues().add(value);
        });
        return sensorData;
    }
    private LocalizationPointDto extractLocalizationData(SurveyParticipation sp){
        if(sp.getLocalizationData() != null){
            return new LocalizationPointDto(sp.getLocalizationData().getLatitude(), sp.getLocalizationData().getLongitude(), sp.getLocalizationData().getDateTime(), sp.getLocalizationData().getOutsideResearchArea(), sp.getLocalizationData().getAccuracyMeters());
        }
        return null;
    }
}
