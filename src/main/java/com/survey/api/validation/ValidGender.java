package com.survey.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = GenderValidator.class)
public @interface ValidGender {
    public String message() default "Invalid gender";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
