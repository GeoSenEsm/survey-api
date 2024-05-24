package com.survey.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = QuestionTypeValidator.class)
public @interface ValidQuestionType {

    public String message() default "Invalid question type.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
