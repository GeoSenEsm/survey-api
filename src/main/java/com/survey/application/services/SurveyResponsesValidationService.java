package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.domain.models.Question;

public interface SurveyResponsesValidationService {
    void validateYesNoAnswerQuestions(Question question, AnswerDto answerDto);
}
