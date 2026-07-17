package com.survey.application.dtos.statistics;

import java.util.UUID;

/**
 * One filled time slot for a respondent. {@code hasLocationData} is true
 * when at least one localization data point was persisted for that
 * respondent inside the slot's {@code [start, finish]} window, and
 * {@code hasSensorData} is the same for sensor readings. The admin
 * "Daily completion" page uses these flags to distinguish between
 * "survey only" and "survey + GPS + sensor" submissions with different
 * colours.
 */
public record DailyCompletionCompletedSlotDto(
        UUID slotId,
        boolean hasLocationData,
        boolean hasSensorData
) {}
