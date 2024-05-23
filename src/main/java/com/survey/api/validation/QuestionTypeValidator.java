package com.survey.api.validation;

import com.survey.domain.models.enums.QuestionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuestionTypeValidator implements ConstraintValidator<ValidQuestionType, String> {

    @Override
    public boolean isValid(String questionType, ConstraintValidatorContext constraintValidatorContext) {
        if (questionType == null){
            return false;
        }

        for (QuestionType q : QuestionType.values()){
            if (q.name().equals(questionType)){
                return true;
            }
        }
        return false;
    }
}
