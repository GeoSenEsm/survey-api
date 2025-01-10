package com.survey.application.dtos.surveyDtos;

import com.survey.application.dtos.SurveySendingPolicyTimesDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Contains a survey with all time slots that it will be available in. Only currently active or upcoming time slots. Does not include the ones from the past.")
public class ResponseSurveyWithTimeSlotsDto {
    private ResponseSurveyDto survey;
    private List<SurveySendingPolicyTimesDto> surveySendingPolicyTimes;
}
