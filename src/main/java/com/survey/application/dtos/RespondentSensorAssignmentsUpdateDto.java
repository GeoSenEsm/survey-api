package com.survey.application.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Write payload for {@code PUT /api/surveysettings/sensordata/assignments}. Kept separate from
 * {@link SurveySensorDataSettingsWriteDto} so this endpoint never inherits the sensor data setup
 * lock: see that DTO's Javadoc for why assignments must stay editable after publish.
 */
public record RespondentSensorAssignmentsUpdateDto(
        @NotNull
        List<@Valid RespondentSensorAssignmentDto> assignments
) {}
