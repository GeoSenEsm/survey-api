package com.survey.api.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SurveyParticipationValidator.class)
public @interface ValidSurveyParticipationId {
    String message() default "Invalid surveyParticipationId or mismatched respondentId";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
