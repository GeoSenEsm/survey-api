package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveyDto;
import com.survey.domain.models.Option;
import com.survey.domain.models.Question;
import com.survey.domain.models.Survey;
import com.survey.domain.models.SurveySection;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


@Service
public class SurveyValidationServiceImpl implements SurveyValidationService{

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

    @Override
    public void validateImageChoiceFiles(CreateSurveyDto survey, List<MultipartFile> files) {
        int imageChoiceOptionsCount = survey.getSections().stream()
                .flatMapToInt(section -> section.getQuestions().stream()
                        .filter(q -> q.getQuestionType().equals(QuestionType.image_choice.name()))
                        .mapToInt(q -> q.getOptions().size()))
                .sum();

        if (files.size() != imageChoiceOptionsCount) {
            throw new IllegalArgumentException("Incorrect number of files uploaded for image choice questions. " +
                    "Expected: " + imageChoiceOptionsCount + ", Found: " + files.size());
        }
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
