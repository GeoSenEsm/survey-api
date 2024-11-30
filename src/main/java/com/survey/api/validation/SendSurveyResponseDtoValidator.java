package com.survey.api.validation;

import com.survey.application.dtos.RespondentGroupDto;
import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.application.dtos.surveyDtos.SelectedOptionDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.RespondentGroupService;
import com.survey.domain.models.*;
import com.survey.domain.repository.OptionRepository;
import com.survey.domain.repository.RespondentDataRepository;
import com.survey.domain.repository.SurveyRepository;
import com.survey.domain.repository.SurveySendingPolicyRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class SendSurveyResponseDtoValidator
implements ConstraintValidator<ValidSendSurveyResponse, SendSurveyResponseDto> {

    private final SurveyRepository surveyRepository;
    private final SurveySendingPolicyRepository surveySendingPolicyRepository;
    private final RespondentGroupService respondentGroupService;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final RespondentDataRepository respondentDataRepository;
    private final OptionRepository optionRepository;




    public SendSurveyResponseDtoValidator(SurveyRepository surveyRepository,
                                          SurveySendingPolicyRepository surveySendingPolicyRepository,
                                          RespondentGroupService respondentGroupService,
                                          ClaimsPrincipalService claimsPrincipalService,
                                          RespondentDataRepository respondentDataRepository,
                                          OptionRepository optionRepository){

        this.surveyRepository = surveyRepository;
        this.surveySendingPolicyRepository = surveySendingPolicyRepository;
        this.respondentGroupService = respondentGroupService;
        this.claimsPrincipalService = claimsPrincipalService;
        this.respondentDataRepository = respondentDataRepository;
        this.optionRepository = optionRepository;
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

        if (!validateAllRequiredQuestionsAnswered(survey, sendSurveyResponseDto.getAnswers(), constraintValidatorContext)) {
            return false;
        }

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
                addConstraintViolation(constraintValidatorContext, "Each answer must match an existing question for specified survey", "answers");
                isValid = false;
                continue;
            }

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

    private boolean isVisibleToParticipant(SurveySection section, List<AnswerDto> answers) {
        return switch (section.getVisibility()) {
            case always -> true;
            case group_specific -> checkGroupSpecificVisibility(section);
            case answer_triggered -> checkAnswerTriggeredVisibility(section, answers);
        };
    }

    private boolean validateAllRequiredQuestionsAnswered(Survey survey, List<AnswerDto> answers, ConstraintValidatorContext ctx) {
        Set<UUID> answeredQuestionIds = answers.stream()
                .map(AnswerDto::getQuestionId)
                .collect(Collectors.toSet());

        boolean allRequiredQuestionsAnswered = survey.getSections().stream()
                .flatMap(section -> section.getQuestions().stream()
                        .filter(question -> question.getRequired() && isVisibleToParticipant(section, answers))
                        .filter(question -> !answeredQuestionIds.contains(question.getId()))
                ).findAny().isEmpty();

        if (!allRequiredQuestionsAnswered) {
            addConstraintViolation(ctx, "All required questions must be answered", "answers");
        }

        return allRequiredQuestionsAnswered;
    }

    private boolean checkGroupSpecificVisibility(SurveySection section) {
        UUID identityUserId = claimsPrincipalService.findIdentityUser().getId();
        UUID respondentId = respondentDataRepository.findByIdentityUserId(identityUserId).getId();
        List<RespondentGroupDto> respondentGroups = respondentGroupService.getRespondentGroups(respondentId);

        List<UUID> respondentGroupIds = respondentGroups.stream()
                .map(RespondentGroupDto::getId)
                .toList();

        return section.getSectionToUserGroups().stream()
                .anyMatch(group -> respondentGroupIds.contains(group.getGroup().getId()));
    }

    private boolean checkAnswerTriggeredVisibility(SurveySection section, List<AnswerDto> answers) {
        List<UUID> selectedOptionIds = new ArrayList<>();
        for (AnswerDto answer : answers) {
            if (answer.getSelectedOptions() != null) {
                for (SelectedOptionDto selectedOption : answer.getSelectedOptions()) {
                    selectedOptionIds.add(selectedOption.getOptionId());
                }
            }
        }
        List<Option> options = optionRepository.findByIdIn(selectedOptionIds);
        for (Option option : options) {
            if (option.getShowSection() != null && option.getShowSection().equals(section.getOrder())) {
                return true;
            }
        }
        return false;
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

