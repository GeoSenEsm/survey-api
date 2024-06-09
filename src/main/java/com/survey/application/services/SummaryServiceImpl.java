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
public class SummaryServiceImpl implements SummaryService {

    private final SurveyParticipationRepository surveyParticipationRepository;
    private final HistogramService histogramService;

    @Autowired
    public SummaryServiceImpl(SurveyParticipationRepository surveyParticipationRepository, HistogramService histogramService) {
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.histogramService = histogramService;
    }

    @Override
    @Transactional
    public List<HistogramDataDto> getHistogramData(UUID surveyId, Date date) {

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date startOfDay = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<SurveyParticipation> surveyParticipationList = surveyParticipationRepository.findAllBySurveyIdAndDate(surveyId, startOfDay);

        if(surveyParticipationList.isEmpty()){
            throw new NoSuchElementException("No survey participation records found for the given survey ID and date.");
        }

        return histogramService.calculateHistogramData(surveyParticipationList);

    }

}
