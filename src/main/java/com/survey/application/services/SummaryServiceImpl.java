package com.survey.application.services;

import com.survey.application.dtos.HistogramDataDto;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.repository.SurveyParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

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
    @Transactional(readOnly = true)
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
