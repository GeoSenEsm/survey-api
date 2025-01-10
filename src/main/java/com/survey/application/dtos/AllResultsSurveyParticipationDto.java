package com.survey.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllResultsSurveyParticipationDto {
    private UUID surveyParticipationId;
    private UUID surveyId;
    private String surveyName;
    private OffsetDateTime responseDate;
    private List<AllResultsQuestionAnswerDto> questionAnswerList;
}
