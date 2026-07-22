package com.survey.domain.models.enums;

/**
 * How the Issues tab resolves each respondent's evaluation window.
 */
public enum IssuesRangeMode {
    /** Use each respondent's assigned survey_start_date / survey_end_date. */
    survey_window,
    /** Use one admin-selected from/to range for every respondent. */
    custom
}
