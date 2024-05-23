package com.survey.api.validation;

import com.survey.domain.models.enums.Gender;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class GenderValidator implements ConstraintValidator<ValidGender, String> {
    @Override
    public boolean isValid(String gender, ConstraintValidatorContext constraintValidatorContext) {
        return gender != null && Arrays.stream(Gender.values())
                .anyMatch(g -> g.name().equals(gender));
    }
}
