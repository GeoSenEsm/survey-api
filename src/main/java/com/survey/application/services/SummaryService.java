package com.survey.application.services;

import com.survey.application.dtos.HistogramDataDto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface SummaryService {
    List<HistogramDataDto> getHistogramData(UUID surveyId, Date date);
}
