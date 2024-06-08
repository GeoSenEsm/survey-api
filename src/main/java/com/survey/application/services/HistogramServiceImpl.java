package com.survey.application.services;

import com.survey.application.dtos.HistogramDataDto;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class HistogramServiceImpl implements HistogramService{

    private ModelMapper modelMapper;
    @Autowired
    public HistogramServiceImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public List<HistogramDataDto> calculateHistogramData(List<SurveyParticipation> surveyParticipationList) {

        Map<UUID, HistogramData> histogramDataMap = initAllQuestionsInSurvey(surveyParticipationList.get(0).getSurvey());

        for (SurveyParticipation participation : surveyParticipationList){
            for (QuestionAnswer answer : participation.getQuestionAnswers()){

                UUID questionId = answer.getQuestion().getId();
                if (answer.getQuestion().getQuestionType() == QuestionType.discrete_number_selection){
                    String selectedNumber = String.valueOf(answer.getNumericAnswer());
                    histogramDataMap.get(questionId).increaseAnswerNumbers(selectedNumber);
                }

                if (answer.getQuestion().getQuestionType() == QuestionType.single_text_selection){
                    String selectedLabel = answer.getOptionSelections().get(0).getOption().getLabel();
                    histogramDataMap.get(questionId).increaseAnswerNumbers(selectedLabel);
                }
            }
        }


        return histogramDataMap.values().stream()
                .map(histogramData -> modelMapper.map(histogramData, HistogramDataDto.class))
                .collect(Collectors.toList());
    }

    private Map<UUID, HistogramData> initAllQuestionsInSurvey(Survey survey){
        Map<UUID, HistogramData> histogramDataMap = new HashMap<>();

        for (SurveySection section : survey.getSections()){
            for(Question question : section.getQuestions()){
                histogramDataMap.put(
                        question.getId(),
                        new HistogramData(question.getContent(), getSeriesForQuestion(question))
                );
            }
        }
        return histogramDataMap;
    }

    List<ChartDataPoint> getSeriesForQuestion(Question question){
        if (question.getQuestionType() == QuestionType.discrete_number_selection){
            NumberRange numberRange = question.getNumberRange();
            return IntStream.rangeClosed(numberRange.getFrom(), numberRange.getTo())
                    .mapToObj(i -> new ChartDataPoint(0, String.valueOf(i)))
                    .collect(Collectors.toList());
        }

        if (question.getQuestionType() == QuestionType.single_text_selection){
            return question.getOptions().stream()
                    .map(option -> new ChartDataPoint(0, option.getLabel()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
