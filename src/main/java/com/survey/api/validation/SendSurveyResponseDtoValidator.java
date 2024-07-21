package com.survey.api.validation;

import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.application.dtos.surveyDtos.SelectedOptionDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.domain.models.Option;
import com.survey.domain.models.Question;
import com.survey.domain.models.Survey;
import com.survey.domain.repository.QuestionAnswerRepository;
import com.survey.domain.repository.SurveyRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


public class SendSurveyResponseDtoValidator
implements ConstraintValidator<SendSurveyResponseDtoValidation, SendSurveyResponseDto> {

    private final SurveyRepository surveyRepository;

    public SendSurveyResponseDtoValidator(SurveyRepository surveyRepository){

        this.surveyRepository = surveyRepository;
    }

    @Override
    public boolean isValid(SendSurveyResponseDto sendSurveyResponseDto, ConstraintValidatorContext constraintValidatorContext) {
        if (sendSurveyResponseDto == null) {
            return true; // null values are handled by @NotNull if needed
        }

        Optional<Survey> surveyOptional = surveyRepository.findById(sendSurveyResponseDto.getSurveyId());

        if (surveyOptional.isEmpty()){
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("This survey does not exist")
                    .addPropertyNode("surveyId")
                    .addConstraintViolation();
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
            answerFoundMappings.put(answerDto.getQuestionId(), true);
            if (!questionIdMappings.containsKey(answerDto.getQuestionId())){
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("Each answer must match an existing question for specified survey")
                        .addPropertyNode("answers")
                        .addConstraintViolation();
                isValid = false;
                continue;
            }

            Question matchingQuestion = questionIdMappings.get(answerDto.getQuestionId());
            if (!validateAnswerWithQuestionType(answerDto, matchingQuestion, constraintValidatorContext)){
                isValid = false;
            }
        }

        if (answerFoundMappings.entrySet().stream()
                .anyMatch(x -> !x.getValue() && questionIdMappings.get(x.getKey()).getRequired())){
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Required questions must have an answer")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            isValid = false;
        }
        return isValid;
    }

    private boolean validateAnswerWithQuestionType(AnswerDto answer,
                                                Question question, ConstraintValidatorContext ctx){
        switch (question.getQuestionType()){
            case yes_no_selection:
                return validateYesNo(answer, ctx);
            case single_text_selection:
                return validateSingleChoice(question, answer, ctx);
            case discrete_number_selection:
                return validateNumberRange(question, answer, ctx);
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean validateYesNo(AnswerDto answerDto, ConstraintValidatorContext ctx){
        boolean result = true;
        if (answerDto.getNumericAnswer() != null){
            ctx
                    .buildConstraintViolationWithTemplate("'Yes/No' answer must not have a numeric value")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        if (answerDto.getSelectedOptions() != null && !answerDto.getSelectedOptions().isEmpty()){
            ctx
                    .buildConstraintViolationWithTemplate("'Yes/No' answer must not have a selected options")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        if (answerDto.getYesNoAnswer() == null){
            ctx
                    .buildConstraintViolationWithTemplate("'Yes/No' answer must have a yes/no answer specified")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        return result;
    }

    private boolean validateNumberRange(Question question, AnswerDto answerDto, ConstraintValidatorContext ctx){
        boolean result = true;
        if (answerDto.getNumericAnswer() == null){
            ctx
                    .buildConstraintViolationWithTemplate("Linear scale answer must have a numeric value")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        } else if(answerDto.getNumericAnswer() > question.getNumberRange().getTo() ||
         answerDto.getNumericAnswer() < question.getNumberRange().getFrom()) {
            ctx
                    .buildConstraintViolationWithTemplate("Answer violates a number range constraint")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        if (answerDto.getSelectedOptions() != null && !answerDto.getSelectedOptions().isEmpty()){
            ctx
                    .buildConstraintViolationWithTemplate("Linear scale answer must not have a selected options")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        if (answerDto.getYesNoAnswer() != null){
            ctx
                    .buildConstraintViolationWithTemplate("Linear scale answer must not have a yes/no answer specified")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        return result;
    }

    private boolean validateSingleChoice(Question question, AnswerDto answerDto,
                                         ConstraintValidatorContext ctx){
        boolean result = true;
        if (answerDto.getNumericAnswer() != null){
            ctx
                    .buildConstraintViolationWithTemplate("Single choice answer must not have a numeric value")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        if (answerDto.getYesNoAnswer() != null){
            ctx
                    .buildConstraintViolationWithTemplate("Single choice answer must not have a yes/no answer specified")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        List<SelectedOptionDto> selectedOptions = answerDto.getSelectedOptions();
        if (selectedOptions == null || selectedOptions.size() != 1){
            ctx
                    .buildConstraintViolationWithTemplate("Single choice answer must not have exactly one selected option")
                    .addPropertyNode("answers")
                    .addConstraintViolation();
            result = false;
        }

        HashSet<UUID> optionsIds = question
                .getOptions()
                .stream()
                .map(Option::getId)
                .collect(Collectors.toCollection(HashSet::new));

        if (selectedOptions.stream().anyMatch(x -> !optionsIds.contains(x.getOptionId()))){
            ctx
                    .buildConstraintViolationWithTemplate("Single choice answer must have a selected option matching available option for the proper question")
                    .addPropertyNode("answers")
                    .addConstraintViolation();

            result = false;
        }
        return result;
    }
}
