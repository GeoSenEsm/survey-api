package com.survey.api.validation;

import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.application.dtos.surveyDtos.SelectedOptionDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.domain.models.*;
import com.survey.domain.repository.SurveyRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class SendSurveyResponseDtoValidator
implements ConstraintValidator<ValidSendSurveyResponse, SendSurveyResponseDto> {

    private final SurveyRepository surveyRepository;

    @Autowired
    public SendSurveyResponseDtoValidator(SurveyRepository surveyRepository){
        this.surveyRepository = surveyRepository;
    }

    @Override
    @Transactional
    public boolean isValid(SendSurveyResponseDto sendSurveyResponseDto, ConstraintValidatorContext constraintValidatorContext) {
        if (
                sendSurveyResponseDto == null ||
                sendSurveyResponseDto.getSurveyId() == null ||
                sendSurveyResponseDto.getStartDate() == null ||
                sendSurveyResponseDto.getFinishDate() == null ||
                sendSurveyResponseDto.getAnswers() == null)
        {
            addConstraintViolation(constraintValidatorContext, "Survey response data is invalid", "surveyId");
            return false;
        }

        Optional<Survey> surveyOptional = surveyRepository.findById(sendSurveyResponseDto.getSurveyId());

        if (surveyOptional.isEmpty()){
            addConstraintViolation(constraintValidatorContext, "This survey does not exist", "surveyId");
            return false;
        }

        Survey survey = surveyOptional.get();
        boolean isValid = true;

        Map<UUID, Question> questionIdMappings =
                survey
                        .getSections()
                        .stream()
                        .flatMap(x -> x.getQuestions().stream())
                        .collect(Collectors.toMap(Question::getId, x -> x));

        Map<UUID, Boolean> answerFoundMappings =
                survey
                        .getSections()
                        .stream()
                        .flatMap(x -> x.getQuestions().stream())
                        .collect(Collectors.toMap(Question::getId, x -> false));

        for (AnswerDto answerDto : sendSurveyResponseDto.getAnswers()){
            if (!questionIdMappings.containsKey(answerDto.getQuestionId())){
                addConstraintViolation(constraintValidatorContext, "Each answer must match an existing question for specified survey", "answers");
                isValid = false;
                break;
            }

            answerFoundMappings.put(answerDto.getQuestionId(), true);

            Question matchingQuestion = questionIdMappings.get(answerDto.getQuestionId());
            if (!validateAnswerWithQuestionType(answerDto, matchingQuestion, constraintValidatorContext)){
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean validateAnswerWithQuestionType(AnswerDto answer, Question question, ConstraintValidatorContext ctx){
        return switch (question.getQuestionType()) {
            case yes_no_choice -> validateYesNoAnswer(answer, ctx);
            case single_choice -> validateChoice(question, answer, ctx, "Single");
            case multiple_choice -> validateChoice(question, answer, ctx, "Multiple");
            case linear_scale -> validateLinearScaleAnswer(question, answer, ctx);
            case number_input -> validateNumericAnswer(answer, ctx, "Numeric answer");
            case image_choice -> validateChoice(question, answer, ctx, "Image");
        };
    }

    private boolean validateNumericAnswer(AnswerDto answerDto, ConstraintValidatorContext ctx, String answerTypeName) {
        boolean result = true;
        if (answerDto.getNumericAnswer() == null){
            addConstraintViolation(ctx, answerTypeName + " must have a numeric value", "answers");
            result = false;
        }

        if (answerDto.getSelectedOptions() != null && !answerDto.getSelectedOptions().isEmpty()){
            addConstraintViolation(ctx, answerTypeName + " must not have a selected options", "answers");
            result = false;
        }

        if (answerDto.getYesNoAnswer() != null){
            addConstraintViolation(ctx, answerTypeName + " answer must not have a yes/no answer specified", "answers");
            result = false;
        }
        return result;
    }

    private boolean validateYesNoAnswer(AnswerDto answerDto, ConstraintValidatorContext ctx){
        boolean result = true;
        if (answerDto.getNumericAnswer() != null){
            addConstraintViolation(ctx, "'Yes/No' answer must not have a numeric value", "answers");
            result = false;
        }

        if (answerDto.getSelectedOptions() != null && !answerDto.getSelectedOptions().isEmpty()){
            addConstraintViolation(ctx, "'Yes/No' answer must not have a selected options", "answers");
            result = false;
        }

        if (answerDto.getYesNoAnswer() == null){
            addConstraintViolation(ctx, "'Yes/No' answer must have a yes/no answer specified", "answers");
            result = false;
        }

        return result;
    }

    private boolean validateLinearScaleAnswer(Question question, AnswerDto answerDto, ConstraintValidatorContext ctx){
        boolean result = validateNumericAnswer(answerDto, ctx, "Linear scale answer");

        if (result) {
            if (answerDto.getNumericAnswer() > question.getNumberRange().getTo() ||
                    answerDto.getNumericAnswer() < question.getNumberRange().getFrom()) {
                addConstraintViolation(ctx, "Answer violates a number range constraint", "answers");
                result = false;
            }
        }
        return result;
    }

    private boolean validateChoice(Question question, AnswerDto answerDto,
                                   ConstraintValidatorContext ctx, String questionTypeName){
        boolean multiple = questionTypeName.equals("Multiple");
        boolean result = true;
        if (answerDto.getNumericAnswer() != null){
            addConstraintViolation(ctx, questionTypeName + " choice answer must not have a numeric value", "answers");
            result = false;
        }

        if (answerDto.getYesNoAnswer() != null){
            addConstraintViolation(ctx, questionTypeName + " choice answer must not have a yes/no answer specified", "answers");
            result = false;
        }

        List<SelectedOptionDto> selectedOptions = answerDto.getSelectedOptions();
        if (!multiple && (selectedOptions == null || selectedOptions.size() != 1)){
            addConstraintViolation(ctx, questionTypeName + " choice answer must have exactly one selected option", "answers");
            result = false;
        }

        if (multiple && (selectedOptions == null || selectedOptions.isEmpty())){
            addConstraintViolation(ctx, questionTypeName + " choice answer must have at least one selected option", "answers");
            result = false;
        }

        HashSet<UUID> optionsIds = question
                .getOptions()
                .stream()
                .map(Option::getId)
                .collect(Collectors.toCollection(HashSet::new));

        if (selectedOptions != null && selectedOptions.stream().anyMatch(x -> !optionsIds.contains(x.getOptionId()))){
            addConstraintViolation(ctx, questionTypeName + " choice answer must have a selected option matching available option for the proper question", "answers");
            result = false;
        }
        return result;
    }

    private void addConstraintViolation(ConstraintValidatorContext ctx, String message, String propertyNode) {
        if (ctx != null) {
            ctx
                    .buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(propertyNode)
                    .addConstraintViolation();
        }
    }

}

