package com.survey.domain.models.enums;

/**
 * How the Issues tab resolves each respondent's evaluation window.
 * In both modes only respondents with an assigned survey window are included.
 */
public enum IssuesRangeMode {
    /** Use each respondent's assigned survey_start_date / survey_end_date. */
    survey_window,
    /**
     * Use one admin-selected from/to range for every respondent that
     * already has a survey window assigned.
     */
    custom
}
