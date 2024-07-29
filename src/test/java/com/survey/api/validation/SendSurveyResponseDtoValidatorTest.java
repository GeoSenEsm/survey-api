package com.survey.api.validation;

import com.survey.application.dtos.SurveySendingPolicyDto;
import com.survey.application.dtos.SurveySendingPolicyTimesDto;
import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.application.dtos.surveyDtos.SelectedOptionDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.application.services.SurveySendingPolicyService;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.repository.SurveyRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SendSurveyResponseDtoValidatorTest {

    @Mock
    private SurveyRepository surveyRepository;
    @Mock
    private SurveySendingPolicyService surveySendingPolicyService;
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
    @MethodSource("getValidDataWithSingleQuestionSurvey")
    void shouldPassWithAnswerMatchingQuestionType(Survey survey, SendSurveyResponseDto response) {
        when(surveyRepository.findById(survey.getId())).thenReturn(Optional.of(survey));
        when(surveySendingPolicyService.getSurveysSendingPolicyById(survey.getId()))
                .thenReturn(List.of(validSurveySendingPolicy(survey.getId())));
        boolean isValid = validator.isValid(response, context);
        assertTrue(isValid);
    }

    public static Stream<Arguments> getValidDataWithSingleQuestionSurvey(){
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
        when(surveySendingPolicyService.getSurveysSendingPolicyById(survey.getId()))
                .thenReturn(List.of(validSurveySendingPolicy(survey.getId())));
        boolean isValid = validator.isValid(response, context);
        assertFalse(isValid);
    }

    public static Stream<Arguments> getInvalidDataWithSingleQuestion(){
        UUID questionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        List<SelectedOptionDto> optionList = Stream.of(new SelectedOptionDto(UUID.randomUUID())).collect(Collectors.toList());
        List<SelectedOptionDto> optionListOfMoreThanOneSelectedOptions = Stream.of(
                new SelectedOptionDto(UUID.randomUUID()),
                new SelectedOptionDto(UUID.randomUUID())
        ).toList();
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
                                QuestionType.yes_no_selection,
                                true,
                                null,
                                null,
                                null
                        ),
                        new AnswerDto(questionId, null, 1, null)
                ),
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
                        new AnswerDto(questionId, optionList, 1, null)
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
                        new AnswerDto(questionId, optionList, null, null)
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
                        new AnswerDto(questionId, null, null, Boolean.FALSE)
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
                        new AnswerDto(questionId, optionList, null, null)
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
                        new AnswerDto(questionId, optionList, 1, null)
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
                        new AnswerDto(questionId, optionList, null, Boolean.FALSE)
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
                        new AnswerDto(questionId, optionListOfMoreThanOneSelectedOptions, null, null)
                )
        );
    }

    @Test
    void shouldFailWhenSurveyDoesNotHaveSendingPolicy() {
        UUID surveyId = UUID.randomUUID();
        SendSurveyResponseDto sendSurveyResponseDto = new SendSurveyResponseDto();
        sendSurveyResponseDto.setSurveyId(surveyId);
        sendSurveyResponseDto.setAnswers(List.of());

        when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(new Survey()));
        when(surveySendingPolicyService.getSurveysSendingPolicyById(surveyId))
                .thenReturn(List.of());

        boolean isValid = validator.isValid(sendSurveyResponseDto, context);

        assertFalse(isValid);
        verify(context).buildConstraintViolationWithTemplate("The survey is not active");
    }

    @Test
    void shouldFailWhenSurveyIsNotActive() {
        UUID surveyId = UUID.randomUUID();
        SendSurveyResponseDto sendSurveyResponseDto = new SendSurveyResponseDto();
        sendSurveyResponseDto.setSurveyId(surveyId);
        sendSurveyResponseDto.setAnswers(List.of());

        SurveySendingPolicyTimesDto pastTimeSlot = new SurveySendingPolicyTimesDto();
        pastTimeSlot.setStart(OffsetDateTime.now().minusDays(2));
        pastTimeSlot.setFinish(OffsetDateTime.now().minusDays(1));

        SurveySendingPolicyDto invalidPolicy = new SurveySendingPolicyDto();
        invalidPolicy.setId(UUID.randomUUID());
        invalidPolicy.setSurveyId(surveyId);
        invalidPolicy.setTimeSlots(List.of(pastTimeSlot));

        when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(new Survey()));
        when(surveySendingPolicyService.getSurveysSendingPolicyById(surveyId))
                .thenReturn(List.of(invalidPolicy));

        boolean isValid = validator.isValid(sendSurveyResponseDto, context);

        assertFalse(isValid);
        verify(context).buildConstraintViolationWithTemplate("The survey is not active");
    }

    private SurveySendingPolicyDto validSurveySendingPolicy(UUID surveyId) {
        SurveySendingPolicyTimesDto pastTimeSlot = new SurveySendingPolicyTimesDto();
        pastTimeSlot.setStart(OffsetDateTime.now().minusDays(2));
        pastTimeSlot.setFinish(OffsetDateTime.now().plusDays(1));

        SurveySendingPolicyDto surveySendingPolicyDto = new SurveySendingPolicyDto();
        surveySendingPolicyDto.setId(UUID.randomUUID());
        surveySendingPolicyDto.setSurveyId(surveyId);
        surveySendingPolicyDto.setTimeSlots(List.of(pastTimeSlot));

        return surveySendingPolicyDto;
    }

}