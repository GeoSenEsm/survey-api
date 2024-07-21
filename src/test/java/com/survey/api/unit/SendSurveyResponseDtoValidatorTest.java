package com.survey.api.unit;

import com.survey.api.validation.SendSurveyResponseDtoValidator;
import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.application.dtos.surveyDtos.SelectedOptionDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SendSurveyResponseDtoValidatorTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext;

    @InjectMocks
    private SendSurveyResponseDtoValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void shouldFailWhenSurveyDoesNotExist() {
        UUID surveyId = UUID.randomUUID();
        SendSurveyResponseDto dto = new SendSurveyResponseDto();
        dto.setSurveyId(surveyId);
        dto.setAnswers(Collections.emptyList());

        boolean isValid = validator.isValid(dto, context);

        assertFalse(isValid);
        verify(context).buildConstraintViolationWithTemplate("This survey does not exist");
    }

    @Test
    void shouldFailWhenAnswerDoesNotMatchExistingQuestion() {
        UUID surveyId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        Survey survey = new Survey();
        survey.setSections(Collections.singletonList(new SurveySection()));
        when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(survey));

        AnswerDto answerDto = new AnswerDto();
        answerDto.setQuestionId(questionId);

        SendSurveyResponseDto dto = new SendSurveyResponseDto();
        dto.setSurveyId(surveyId);
        dto.setAnswers(Collections.singletonList(answerDto));

        boolean isValid = validator.isValid(dto, context);

        assertFalse(isValid);
    }

    @ParameterizedTest
    @MethodSource("getValidData")
    void shouldPassWithAnswerMatchingQuestionType(Survey survey, SendSurveyResponseDto response) {
        when(surveyRepository.findById(survey.getId())).thenReturn(Optional.of(survey));
        boolean isValid = validator.isValid(response, context);
        assertTrue(isValid);
    }

    public static Stream<Arguments> getValidData(){
        UUID questionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        return Stream.of(
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.yes_no_selection,
                                true,
                                null,
                                null,
                                null
                        ),
                        new AnswerDto(questionId, null, null, true)
                ),
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.discrete_number_selection,
                                true,
                                null,
                                null,
                                new NumberRange(
                                        UUID.randomUUID(),
                                        1,
                                        5,
                                        null, null,
                                        null, null
                                )
                        ),
                        new AnswerDto(questionId, null, 3, null)
                ),
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.single_text_selection,
                                true,
                                null,
                                Stream.of(
                                        new Option(
                                                optionId,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                        )
                                ).collect(Collectors.toList()),
                                null
                        ),
                        new AnswerDto(questionId, Stream.of(new SelectedOptionDto(optionId)).collect(Collectors.toList()), null, null)
                )
        );
    }

    private static Arguments getArgumentsWithSingleQuestionSurvey(Question question, AnswerDto answerDto){
        UUID surveyId = UUID.randomUUID();
        return  Arguments.of(
                new Survey(
                        surveyId,
                        "name",
                        null,
                        Collections.singletonList(
                                new SurveySection(
                                        UUID.randomUUID(),
                                        null,
                                        null,
                                        null,
                                        Visibility.always,
                                        null,
                                        Stream.of(
                                                question
                                        ).collect(Collectors.toList()),
                                        null
                                )
                        ),
                        null
                ),
                new SendSurveyResponseDto(
                        surveyId,
                        Stream.of(
                                answerDto
                        ).collect(Collectors.toList())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidDataWithSingleQuestion")
    void shouldFailWhenSingleAnswerIsInvalidOrMissing(Survey survey, SendSurveyResponseDto response) {
        when(surveyRepository.findById(survey.getId())).thenReturn(Optional.of(survey));
        boolean isValid = validator.isValid(response, context);
        assertFalse(isValid);
    }

    public static Stream<Arguments> getInvalidDataWithSingleQuestion(){
        UUID questionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        return Stream.of(
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.yes_no_selection,
                                true,
                                null,
                                null,
                                null
                        ),
                        new AnswerDto(questionId, null, null, null)
                ),
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.discrete_number_selection,
                                true,
                                null,
                                null,
                                new NumberRange(
                                        UUID.randomUUID(),
                                        1,
                                        5,
                                        null, null,
                                        null, null
                                )
                        ),
                        new AnswerDto(questionId, null, 6, null)
                ),
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.discrete_number_selection,
                                true,
                                null,
                                null,
                                new NumberRange(
                                        UUID.randomUUID(),
                                        1,
                                        5,
                                        null, null,
                                        null, null
                                )
                        ),
                        new AnswerDto(questionId, null, 0, null)
                ),
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.discrete_number_selection,
                                true,
                                null,
                                null,
                                new NumberRange(
                                        UUID.randomUUID(),
                                        1,
                                        5,
                                        null, null,
                                        null, null
                                )
                        ),
                        new AnswerDto(questionId, null, null, null)
                ),
                getArgumentsWithSingleQuestionSurvey(
                        new Question(
                                questionId,
                                null,
                                null,
                                null,
                                QuestionType.single_text_selection,
                                true,
                                null,
                                Stream.of(
                                        new Option(
                                                optionId,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                        )
                                ).collect(Collectors.toList()),
                                null
                        ),
                        new AnswerDto(questionId, Stream.of(new SelectedOptionDto(UUID.randomUUID())).collect(Collectors.toList()), null, null)
                )
        );
    }
}

