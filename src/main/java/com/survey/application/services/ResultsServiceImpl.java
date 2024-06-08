package com.survey.application.services;

import com.survey.application.dtos.HistogramDataDto;
import com.survey.domain.models.*;
import com.survey.domain.repository.SurveyParticipationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ResultsServiceImpl implements ResultsService {

    private final SurveyParticipationRepository surveyParticipationRepository;
    private final HistogramService histogramService;

    @Autowired
    public ResultsServiceImpl(SurveyParticipationRepository surveyParticipationRepository, HistogramService histogramService) {
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.histogramService = histogramService;
    }

    @Override
    @Transactional
    public List<HistogramDataDto> getHistogramData(UUID surveyId, LocalDate localDate) {
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<SurveyParticipation> surveyParticipationList = surveyParticipationRepository.findAllBySurveyIdAndDate(surveyId, date);

        return histogramService.calculateHistogramData(surveyParticipationList);

    }

}
