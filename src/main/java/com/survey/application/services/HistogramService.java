package com.survey.application.services;

import com.survey.application.dtos.HistogramDataDto;
import com.survey.domain.models.SurveyParticipation;

import java.util.List;

public interface HistogramService {
    List<HistogramDataDto> calculateHistogramData(List<SurveyParticipation> surveyParticipation);
}
