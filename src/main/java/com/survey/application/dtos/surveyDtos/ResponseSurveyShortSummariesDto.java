package com.survey.application.dtos.surveyDtos;

import com.survey.application.dtos.SurveySendingPolicyTimesDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseSurveyShortSummariesDto extends ResponseSurveyShortDto{
    private List<SurveySendingPolicyTimesDto> dates;
}
