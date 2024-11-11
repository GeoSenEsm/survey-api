package com.survey.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = TimeSlotValidator.class)
public @interface ValidTimeSlotIds {
    String message() default "One or more IDs do not exist in the database.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
