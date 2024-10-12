package com.survey.application.services;

import com.survey.application.dtos.SurveyResultDto;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SurveyResponsesServiceImpl.class)
class SurveyResponsesServiceImplTest {

    private static final String SURVEY_NAME = "Survey 1";
    private static final String QUESTION_1 = "Question 1";
    private static final String QUESTION_2 = "Question 2";
    private static final String QUESTION_3 = "Question 3";
    private static final String OPTION_1 = "Option 1";
    private static final String OPTION_2 = "Option 2";
    private static final boolean YES_NO_ANSWER = true;
    private static final int NUMERIC_ANSWER = 4;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SurveyResponsesServiceImpl surveyResponsesService;
    private Survey survey;
    private IdentityUser user;
    private List<QuestionAnswer> questionAnswerList;
    private SurveyParticipation surveyParticipation;

    @BeforeEach
    public void setup() {
        survey = createSurvey();
        user = createIdentityUser();
        questionAnswerList = createQuestionAnswerList();
        surveyParticipation = createSurveyParticipation();
    }

    @Test
    void shouldGetSurveyResults() {
        UUID surveyId = survey.getId();
        OffsetDateTime dateFrom = OffsetDateTime.now(ZoneOffset.UTC).minusYears(1);
        OffsetDateTime dateTo = OffsetDateTime.now(ZoneOffset.UTC).plusYears(1);

        TypedQuery<SurveyParticipation> mockQuery = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(SurveyParticipation.class)))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList())
                .thenReturn(List.of(surveyParticipation));

        List<SurveyResultDto> results = surveyResponsesService.getSurveyResults(surveyId, dateFrom, dateTo);

        assertEquals(SURVEY_NAME, results.get(0).getSurveyName());
        assertEquals(QUESTION_1, results.get(0).getQuestion());
        assertEquals(OPTION_1, results.get(0).getAnswers().get(0));

        assertEquals(SURVEY_NAME, results.get(1).getSurveyName());
        assertEquals(QUESTION_2, results.get(1).getQuestion());
        assertEquals(YES_NO_ANSWER, results.get(1).getAnswers().get(0));

        assertEquals(SURVEY_NAME, results.get(2).getSurveyName());
        assertEquals(QUESTION_3, results.get(2).getQuestion());
        assertEquals(NUMERIC_ANSWER, results.get(2).getAnswers().get(0));

        verify(mockQuery).setParameter("surveyId", surveyId);
        verify(mockQuery).setParameter("dateFrom", dateFrom);
        verify(mockQuery).setParameter("dateTo", dateTo);
    }

    @Test
    void shouldNotReturnSurveyResultsWhenDateIsOutOfRange() {
        UUID surveyId = survey.getId();
        OffsetDateTime dateFrom = OffsetDateTime.now(ZoneOffset.UTC).minusYears(2);
        OffsetDateTime dateTo = OffsetDateTime.now(ZoneOffset.UTC).minusYears(1);

        TypedQuery<SurveyParticipation> mockQuery = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(SurveyParticipation.class)))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList())
                .thenReturn(List.of());

        List<SurveyResultDto> results = surveyResponsesService.getSurveyResults(surveyId, dateFrom, dateTo);

        assertTrue(results.isEmpty());

        verify(mockQuery).setParameter("surveyId", surveyId);
        verify(mockQuery).setParameter("dateFrom", dateFrom);
        verify(mockQuery).setParameter("dateTo", dateTo);
    }

    private SurveyParticipation createSurveyParticipation() {
        SurveyParticipation participation = new SurveyParticipation();
        participation.setSurvey(survey);
        participation.setDate(OffsetDateTime.now(ZoneOffset.UTC));
        participation.setIdentityUser(user);
        participation.setQuestionAnswers(questionAnswerList);
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
        question1.setQuestionType(QuestionType.single_text_selection);
        question1.setOptions(List.of(option1, option2));

        Question question2 = new Question();
        question2.setContent(QUESTION_2);
        question2.setQuestionType(QuestionType.yes_no_selection);

        Question question3 = new Question();
        question3.setContent(QUESTION_3);
        question3.setQuestionType(QuestionType.discrete_number_selection);
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