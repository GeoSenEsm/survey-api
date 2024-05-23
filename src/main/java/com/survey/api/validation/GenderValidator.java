package com.survey.api.validation;

import com.survey.domain.models.enums.Gender;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GenderValidator implements ConstraintValidator<ValidGender, String> {
    @Override
    public boolean isValid(String gender, ConstraintValidatorContext constraintValidatorContext) {
        if (gender == null){
            return false;
        }

        for (Gender g : Gender.values()){
            if (g.name().equals(gender)){
                return true;
            }
        }

        return false;
    }
}
