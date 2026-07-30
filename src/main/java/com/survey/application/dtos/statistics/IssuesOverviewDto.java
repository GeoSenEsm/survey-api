package com.survey.application.dtos.statistics;

import java.util.List;

/**
 * Issues overview for the admin Issues tab: per-respondent fulfillment metrics.
 */
public record IssuesOverviewDto(
        List<RespondentIssueDto> respondents,
        long respondentsConsidered
) {}
