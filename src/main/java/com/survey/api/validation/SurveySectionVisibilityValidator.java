package com.survey.api.validation;

import com.survey.domain.models.enums.Visibility;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class SurveySectionVisibilityValidator implements ConstraintValidator<ValidVisibility, String> {

    @Override
    public boolean isValid(String visibility, ConstraintValidatorContext constraintValidatorContext) {
        return visibility != null && Arrays.stream(Visibility.values())
                .anyMatch(v -> v.name().equals(visibility));
    }

}
