package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.AnswerDto;
import com.survey.application.dtos.surveyDtos.SendSurveyResponseDto;
import com.survey.application.dtos.surveyDtos.SurveyParticipationDto;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class SurveyResponsesServiceImpl implements SurveyResponsesService {
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final SurveyRepository surveyRepository;
    private final OptionRepository optionRepository;
    private final QuestionRepository questionRepository;
    private final ClaimsPrincipalServiceImpl claimsPrincipalServiceImpl;
    private final IdentityUserRepository identityUserRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;


    @Autowired
    public SurveyResponsesServiceImpl(
            SurveyParticipationRepository surveyParticipationRepository,
            SurveyRepository surveyRepository,
            OptionRepository optionRepository,
            QuestionRepository questionRepository,
            ClaimsPrincipalServiceImpl claimsPrincipalServiceImpl,
            IdentityUserRepository identityUserRepository,
            ModelMapper modelMapper,
            EntityManager entityManager) {
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.surveyRepository = surveyRepository;
        this.optionRepository = optionRepository;
        this.questionRepository = questionRepository;
        this.claimsPrincipalServiceImpl = claimsPrincipalServiceImpl;
        this.identityUserRepository = identityUserRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }
    private IdentityUser findIdentityUserFromToken(String token) {
        String usernameFromJwt = claimsPrincipalServiceImpl.getCurrentUsername(token);
        if (usernameFromJwt == null){
            throw new BadCredentialsException("Invalid credentials");
        }
        return identityUserRepository.findByUsername(usernameFromJwt)
                .orElseThrow(() -> new IllegalArgumentException("Invalid respondent ID - respondent doesn't exist"));
    }

    private Survey findSurveyById(UUID surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid survey ID - survey doesn't exist"));
    }

    private Question findQuestionById(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question ID - question doesn't exist"));
    }

    private Option findOptionById(UUID optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid option ID - option doesn't exist"));
    }

    private SurveyParticipation saveSurveyParticipation(SendSurveyResponseDto sendSurveyResponseDto, IdentityUser identityUser) {
        SurveyParticipation surveyParticipation = new SurveyParticipation();
        surveyParticipation.setIdentityUser(identityUser);
        surveyParticipation.setDate(new Date());
        surveyParticipation.setSurvey(findSurveyById(sendSurveyResponseDto.getSurveyId()));
        return surveyParticipationRepository.save(surveyParticipation);
    }

    private QuestionAnswer createQuestionAnswer(SurveyParticipation surveyParticipation, AnswerDto answerDto) {
        Question question = findQuestionById(answerDto.getQuestionId());
        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setSurveyParticipation(surveyParticipation);
        questionAnswer.setQuestion(question);

        if (question.getQuestionType().equals(QuestionType.discrete_number_selection)) {
            int numericAnswer = answerDto.getNumericAnswer();
            if (numericAnswer == 0) {
                throw new IllegalArgumentException("Numeric answer cannot be null or empty for discrete number selection questions.");
            }
            questionAnswer.setNumericAnswer(numericAnswer);
        } else {
            if (answerDto.getSelectedOptions() == null || answerDto.getSelectedOptions().isEmpty()) {
                throw new IllegalArgumentException("Option selections cannot be empty for single text selection questions.");
            }
            List<OptionSelection> optionSelections = answerDto.getSelectedOptions().stream()
                    .map(selectedOptionDto -> {
                        Option option = findOptionById(selectedOptionDto.getOptionId());
                        OptionSelection optionSelection = new OptionSelection();
                        optionSelection.setQuestionAnswer(questionAnswer);
                        optionSelection.setOption(option);
                        return optionSelection;
                    }).collect(Collectors.toList());
            questionAnswer.setOptionSelections(optionSelections);
        }

        return questionAnswer;
    }

    private SurveyParticipation mapQuestionAnswers(SendSurveyResponseDto sendSurveyResponseDto, SurveyParticipation surveyParticipation) {
        List<QuestionAnswer> questionAnswers = sendSurveyResponseDto.getAnswers().stream()
                .map(answerDto -> createQuestionAnswer(surveyParticipation, answerDto))
                .collect(Collectors.toList());

        surveyParticipation.setQuestionAnswers(questionAnswers);
        return surveyParticipation;
    }

    private SurveyParticipationDto mapToDto(SurveyParticipation surveyParticipation, SendSurveyResponseDto sendSurveyResponseDto, IdentityUser identityUser) {
        SurveyParticipation finalSurveyParticipation = surveyParticipationRepository.saveAndFlush(surveyParticipation);
        entityManager.refresh(finalSurveyParticipation);
        SurveyParticipationDto surveyParticipationDto = modelMapper.map(finalSurveyParticipation, SurveyParticipationDto.class);
        surveyParticipationDto.setSurveyId(sendSurveyResponseDto.getSurveyId());
        surveyParticipationDto.setRespondentId(identityUser.getId());
        return surveyParticipationDto;
    }

    @Override
    @Transactional
    public SurveyParticipationDto saveSurveyResponse(SendSurveyResponseDto sendSurveyResponseDto, String token) {
        IdentityUser identityUser = findIdentityUserFromToken(token);
        SurveyParticipation surveyParticipation = saveSurveyParticipation(sendSurveyResponseDto, identityUser);
        SurveyParticipation finalSurveyParticipation = mapQuestionAnswers(sendSurveyResponseDto, surveyParticipation);
        return mapToDto(finalSurveyParticipation, sendSurveyResponseDto, identityUser);
    }
}
