package com.survey.api.validation;

import com.survey.domain.models.enums.Visibility;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SurveySectionVisibilityValidator implements ConstraintValidator<ValidVisibility, String> {

    @Override
    public boolean isValid(String visibility, ConstraintValidatorContext constraintValidatorContext) {
        if (visibility == null){
            return false;
        }

        for (Visibility v : Visibility.values()){
            if (v.name().equals(visibility)){
                return true;
            }
        }
        return false;
    }

}
