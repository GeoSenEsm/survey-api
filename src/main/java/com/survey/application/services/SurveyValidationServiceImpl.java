package com.survey.application.services;

import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.repository.RespondentGroupRepository;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class SurveyValidationServiceImpl implements SurveyValidationService{
    private final RespondentGroupRepository respondentGroupRepository;

    public SurveyValidationServiceImpl(RespondentGroupRepository respondentGroupRepository) {
        this.respondentGroupRepository = respondentGroupRepository;
    }

    @Override
    public void validateShowSections(Survey survey) {

        Dictionary<Integer, Visibility> sectionOrderToVisibility = new Hashtable<>();
        List<SurveySection> sections = survey.getSections();

        sections.forEach(section -> sectionOrderToVisibility.put(section.getOrder(), section.getVisibility()));

        sections.forEach(section -> {
            List<Question> singleTextSelectionQuestions = getQuestionsByType(section, QuestionType.single_choice);
            singleTextSelectionQuestions.forEach(question ->
                    question.getOptions().forEach(option ->
                            validateOptionShowSection(option, sectionOrderToVisibility, section.getOrder())
                    )
            );
        });

    }


    private List<Question> getQuestionsByType(SurveySection section, QuestionType questionType) {
        List<Question> questionsByType = new ArrayList<>();
        for (Question question : section.getQuestions()) {
            if (question.getQuestionType() == questionType) {
                questionsByType.add(question);
            }
        }
        return questionsByType;
    }

    private void validateOptionShowSection(Option option, Dictionary<Integer, Visibility> sectionOrderToVisibility, Integer currentSectionOrder) {
        if (option.getShowSection() != null) {
            validateSingleShowSection(option.getShowSection(), sectionOrderToVisibility, currentSectionOrder);
        }
    }

    private void validateSingleShowSection(Integer showSection, Dictionary<Integer, Visibility> sectionOrderToVisibility, Integer currentSectionOrder){
        if (sectionOrderToVisibility.get(showSection) == null){
            throw new IllegalArgumentException("showSection: " + showSection + ". Section with this order does not exist.");
        }
        if (showSection.equals(currentSectionOrder)){
            throw new IllegalArgumentException("ShowSection can not point to the current section.");
        }
        if (sectionOrderToVisibility.get(showSection) != Visibility.answer_triggered){
            throw new IllegalArgumentException("Section with order: " + showSection + " must have answer_triggered visibility");
        }
    }


}
