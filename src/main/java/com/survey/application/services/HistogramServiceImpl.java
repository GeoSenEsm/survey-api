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

    private final ModelMapper modelMapper;
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


        var data = histogramDataMap.values().stream()
                .sorted(Comparator.comparingInt(HistogramData::getOrder))
                .map(histogramData -> modelMapper.map(histogramData, HistogramDataDto.class))
                .collect(Collectors.toList());

        return data;
    }

    private Map<UUID, HistogramData> initAllQuestionsInSurvey(Survey survey){
        Map<UUID, HistogramData> histogramDataMap = new HashMap<>();

        List<SurveySection> sections = survey.getSections();
        int histogramDataNumber = 0;
        Collections.sort(sections, new Comparator<SurveySection>() {
            @Override
            public int compare(SurveySection o1, SurveySection o2) {
                if (o1.getOrder() > o2.getOrder()){
                    return 1;
                }

                if (o1.getOrder() == o2.getOrder()){
                    return 0;
                }

                return -1;
            }
        });

        for (SurveySection section : sections){
            //TODO: remove this n + 1 issue
            List<Question> questions = section.getQuestions();

            Collections.sort(questions, new Comparator<Question>() {
                @Override
                public int compare(Question o1, Question o2) {
                    //TODO: dry this, maybe some interface like Orderable to be implemented in Section and Question? They both have order
                    if (o1.getOrder() > o2.getOrder()){
                        return 1;
                    }

                    if (o1.getOrder() == o2.getOrder()){
                        return 0;
                    }

                    return -1;
                }
            });
            for(Question question : questions){
                histogramDataMap.put(
                        question.getId(),
                        new HistogramData(question.getContent(), histogramDataNumber,  getSeriesForQuestion(question))
                );
                histogramDataNumber++;
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
