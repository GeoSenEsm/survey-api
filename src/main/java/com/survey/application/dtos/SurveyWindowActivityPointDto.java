package com.survey.application.dtos;

import java.time.LocalDate;

/** One (calendar day, active-respondent count) point for the assignment chart. */
public record SurveyWindowActivityPointDto(LocalDate date, long activeCount) {}
