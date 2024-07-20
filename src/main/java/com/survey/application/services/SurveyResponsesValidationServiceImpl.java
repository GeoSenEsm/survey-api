package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.domain.models.Question;
import org.springframework.stereotype.Service;

@Service
public class SurveyResponsesValidationServiceImpl implements SurveyResponsesValidationService {
    @Override
    public void validateYesNoAnswerQuestions(Question question, AnswerDto answerDto) {
        if (answerDto.getNumericAnswer() != null) {
            throw new IllegalArgumentException("Numeric answer is not allowed for yes/no questions.");
        }
        if (answerDto.getSelectedOptions() != null) {
            throw new IllegalArgumentException("Options are not allowed for yes/no questions.");
        }
        if(answerDto.getYesNoAnswer() == null) {
            throw new IllegalArgumentException(String.format("No answer for yes/no question: %s", question.getId()));
        }
    }
}
