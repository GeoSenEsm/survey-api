package com.survey.application.dtos;

import com.survey.application.dtos.surveyDtos.SendOnlineSurveyResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SendOnlineSurveyResponseDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void sensorDataIsCascadeValidated() throws NoSuchFieldException {
        Valid annotation = SendOnlineSurveyResponseDto.class
                .getDeclaredField("sensorData")
                .getAnnotation(Valid.class);

        assertNotNull(annotation);
    }

    @Test
    void sensorDataRejectsEmptyValues() {
        SensorDataDto sensorData = new SensorDataDto(
                OffsetDateTime.now(),
                "xiaomi",
                List.of());

        assertTrue(validator.validate(sensorData).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("values")));
    }
}
