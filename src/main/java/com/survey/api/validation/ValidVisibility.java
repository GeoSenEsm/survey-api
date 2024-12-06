package com.survey.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SurveySectionVisibilityValidator.class)
public @interface ValidVisibility {

    public String message() default "Invalid survey visibility.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
