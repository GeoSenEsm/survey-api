package com.survey.application.services;

import com.survey.api.validation.SendSurveyResponseDtoValidator;
import com.survey.application.dtos.SurveyResultDto;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyResponsesServiceImplTest {

    private static final String SURVEY_NAME = "Survey 1";
    private static final String QUESTION_1 = "Question 1";
    private static final String QUESTION_2 = "Question 2";
    private static final String QUESTION_3 = "Question 3";
    private static final String OPTION_1 = "Option 1";
    private static final String OPTION_2 = "Option 2";
    private static final BigDecimal VALID_LATITUDE = new BigDecimal("52.237049");
    private static final BigDecimal VALID_LONGITUDE = new BigDecimal("21.017532");

    @Mock
    private SurveyParticipationRepository surveyParticipationRepository;

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ClaimsPrincipalServiceImpl claimsPrincipalServiceImpl;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SendSurveyResponseDtoValidator sendSurveyResponseDtoValidator;

    @Mock
    private SurveyParticipationTimeValidationService surveyParticipationTimeValidationService;

    @Mock
    private SensorDataRepository sensorDataRepository;

    @Mock
    private IdentityUserRepository identityUserRepository;

    @Mock
    private LocalizationDataRepository localizationDataRepository;

    private SurveyResponsesServiceImpl surveyResponsesService;

    private Survey survey;
    private IdentityUser user;
    private List<QuestionAnswer> questionAnswerList;
    private LocalizationData localizationData;
    private SurveyParticipation surveyParticipation;

    @BeforeEach
    void setup() {
        assertNotNull(surveyParticipationRepository);

        surveyResponsesService = new SurveyResponsesServiceImpl(
                surveyParticipationRepository,
                surveyRepository,
                optionRepository,
                questionRepository,
                claimsPrincipalServiceImpl,
                modelMapper,
                entityManager,
                sendSurveyResponseDtoValidator,
                surveyParticipationTimeValidationService,
                sensorDataRepository,
                identityUserRepository,
                localizationDataRepository
        );

        survey = createSurvey();
        user = createIdentityUser();
        questionAnswerList = createQuestionAnswerList();
        localizationData = createLocalizationData();
        surveyParticipation = createSurveyParticipation();
    }

    @Test
    void getSurveyResults_shouldReturnAllSurveyResults_WhenNoParams() {
        // given
        when(surveyParticipationRepository.findByFiltersWithFetch(null, null, null, null, null))
                .thenReturn(List.of(surveyParticipation));

        // when
        List<SurveyResultDto> results = surveyResponsesService.getSurveyResults(null, null, null, null, null);

        // then
        assertFalse(results.isEmpty());
        assertEquals(SURVEY_NAME, results.get(0).getSurveyName());
        assertEquals(user.getId(), results.get(0).getRespondentId());
    }

    private SurveyParticipation createSurveyParticipation() {
        SurveyParticipation participation = new SurveyParticipation();
        participation.setId(UUID.randomUUID());
        participation.setSurvey(survey);
        participation.setDate(OffsetDateTime.now(ZoneOffset.UTC));
        participation.setIdentityUser(user);
        participation.setQuestionAnswers(questionAnswerList);
        participation.setLocalizationData(localizationData);
        return participation;
    }

    private Survey createSurvey() {
        Survey survey = new Survey();
        survey.setId(UUID.randomUUID());
        survey.setName(SURVEY_NAME);
        return survey;
    }

    private IdentityUser createIdentityUser() {
        IdentityUser user = new IdentityUser();
        user.setId(UUID.randomUUID());
        return user;
    }

    private LocalizationData createLocalizationData() {
        LocalizationData localizationData = new LocalizationData();
        localizationData.setLatitude(VALID_LATITUDE);
        localizationData.setLongitude(VALID_LONGITUDE);
        localizationData.setDateTime(OffsetDateTime.now());
        localizationData.setOutsideResearchArea(false);
        localizationData.setAccuracyMeters(BigDecimal.ONE);
        return localizationData;
    }

    private List<QuestionAnswer> createQuestionAnswerList() {
        Option option1 = new Option();
        option1.setId(UUID.randomUUID());
        option1.setLabel(OPTION_1);

        Option option2 = new Option();
        option2.setId(UUID.randomUUID());
        option2.setLabel(OPTION_2);

        NumberRange numberRange = new NumberRange();
        numberRange.setFrom(1);
        numberRange.setTo(5);

        Question question1 = new Question();
        question1.setContent(QUESTION_1);
        question1.setQuestionType(QuestionType.single_choice);
        question1.setOptions(List.of(option1, option2));

        Question question2 = new Question();
        question2.setContent(QUESTION_2);
        question2.setQuestionType(QuestionType.yes_no_choice);

        Question question3 = new Question();
        question3.setContent(QUESTION_3);
        question3.setQuestionType(QuestionType.linear_scale);
        question3.setNumberRange(numberRange);

        OptionSelection optionSelection = new OptionSelection();
        optionSelection.setOption(option1);

        QuestionAnswer questionAnswer1 = new QuestionAnswer();
        questionAnswer1.setQuestion(question1);
        questionAnswer1.setOptionSelections(List.of(optionSelection));

        QuestionAnswer questionAnswer2 = new QuestionAnswer();
        questionAnswer2.setQuestion(question2);
        questionAnswer2.setYesNoAnswer(true);

        QuestionAnswer questionAnswer3 = new QuestionAnswer();
        questionAnswer3.setQuestion(question3);
        questionAnswer3.setNumericAnswer(4);

        return List.of(questionAnswer1, questionAnswer2, questionAnswer3);
    }

}