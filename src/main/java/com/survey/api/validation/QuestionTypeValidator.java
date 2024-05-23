package com.survey.api.validation;

import com.survey.domain.models.enums.QuestionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class QuestionTypeValidator implements ConstraintValidator<ValidQuestionType, String> {

    @Override
    public boolean isValid(String questionType, ConstraintValidatorContext constraintValidatorContext) {
        return questionType != null && Arrays.stream(QuestionType.values())
                .anyMatch(q -> q.name().equals(questionType));
    }
}
