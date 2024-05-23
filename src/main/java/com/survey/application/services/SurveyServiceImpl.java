package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.*;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.IllegalFormatCodePointException;
import java.util.Optional;
import java.util.UUID;

@Service
public class SurveyServiceImpl implements SurveyService{

    private final SurveyRepository surveyRepository;
    private final SurveySectionRepository surveySectionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    private final RespondentGroupRepository respondentGroupRepository;
    private final SectionToUserGroupRepository sectionToUserGroupRepository;

    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, SurveySectionRepository surveySectionRepository, QuestionRepository questionRepository, OptionRepository optionRepository, RespondentGroupRepository respondentGroupRepository, SectionToUserGroupRepository sectionToUserGroupRepository) {
        this.surveyRepository = surveyRepository;
        this.surveySectionRepository = surveySectionRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.respondentGroupRepository = respondentGroupRepository;
        this.sectionToUserGroupRepository = sectionToUserGroupRepository;
    }

    @Override
    public void createSurvey(CreateSurveyRequestDto createSurveyRequestDto) {
        Survey survey = new Survey();
        survey.setName(createSurveyRequestDto.getSurvey().getName());
        surveyRepository.save(survey);

        for (SurveySectionDto sectionDto : createSurveyRequestDto.getSurveySection()){
            SurveySection section = new SurveySection();
            section.setOrder(sectionDto.getOrder());
            section.setName(sectionDto.getName());
            section.setVisibility(Visibility.valueOf(sectionDto.getVisibility()));
            section.setSurvey(survey);
            surveySectionRepository.save(section);

            if (sectionDto.getGroupId() != null){
                Optional<RespondentGroup> optionalRespondentGroup = respondentGroupRepository.findById(UUID.fromString(sectionDto.getGroupId()));
                if (optionalRespondentGroup.isPresent()){
                    SectionToUserGroup sectionToUserGroup = new SectionToUserGroup();
                    sectionToUserGroup.setSection(section);
                    sectionToUserGroup.setGroup(optionalRespondentGroup.get());
                    sectionToUserGroupRepository.save(sectionToUserGroup);
                }
                else {
                    throw new IllegalArgumentException("Respondent group not found for ID: " + sectionDto.getGroupId());
                }
            }


            for (QuestionDto questionDto : sectionDto.getQuestions()){
                Question question = new Question();
                question.setOrder(questionDto.getOrder());
                question.setContent(questionDto.getContent());
                question.setQuestionType(QuestionType.valueOf(questionDto.getQuestionType()));
                question.setRequired(questionDto.isRequired());
                question.setSection(section);
                questionRepository.save(question);

                for (OptionDto optionDto : questionDto.getOptions()){
                    Option option = new Option();
                    option.setOrder(optionDto.getOrder());
                    option.setLabel(optionDto.getLabel());
                    option.setQuestion(question);
                    optionRepository.save(option);
                }
            }
        }


    }
}
