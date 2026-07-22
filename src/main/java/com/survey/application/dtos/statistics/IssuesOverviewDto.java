package com.survey.application.dtos.statistics;

import java.util.List;

/**
 * Issues overview for the admin Issues tab: per-respondent fulfillment
 * metrics plus counts of respondents below the 80% threshold.
 */
public record IssuesOverviewDto(
        List<RespondentIssueDto> respondents,
        long below80SurveyCount,
        long below80GpsCount,
        long below80SensorCount,
        long respondentsConsidered
) {}
