package com.survey.application.services;

import com.survey.application.dtos.HistogramDataDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ResultsService {
    List<HistogramDataDto> getHistogramData(UUID surveyId, LocalDate date);
}
