package com.survey.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SendSurveyResponseDtoValidator.class)
public @interface ValidSendSurveyResponse {
    String message() default "Invalid survey response";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
