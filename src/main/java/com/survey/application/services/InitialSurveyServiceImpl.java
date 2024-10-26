package com.survey.application.services;

import com.survey.application.dtos.initialSurvey.*;
import com.survey.domain.models.InitialSurvey;
import com.survey.domain.models.InitialSurveyOption;
import com.survey.domain.models.InitialSurveyQuestion;
import com.survey.domain.repository.InitialSurveyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class InitialSurveyServiceImpl implements InitialSurveyService {

    private final InitialSurveyRepository initialSurveyRepository;
    private final ModelMapper modelMapper;
    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public InitialSurveyServiceImpl(InitialSurveyRepository initialSurveyQuestionRepository, ModelMapper modelMapper, EntityManager entityManager) {
        this.initialSurveyRepository = initialSurveyQuestionRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<InitialSurveyQuestionResponseDto> createInitialSurvey(List<CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList) {
        InitialSurvey initialSurvey = mapToInitialSurvey(createInitialSurveyQuestionDtoList);
        InitialSurvey dbInitialSurvey = initialSurveyRepository.saveAndFlush(initialSurvey);
        entityManager.refresh(dbInitialSurvey);
        return mapToInitialSurveyResponseDto(dbInitialSurvey);
    }


    @Override
    @Transactional
    public List<InitialSurveyQuestionResponseDto> getInitialSurvey() {
        String queryStr = "SELECT i FROM InitialSurvey i";
        InitialSurvey initialSurvey = entityManager.createQuery(queryStr, InitialSurvey.class)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No initial survey created"));
        return mapToInitialSurveyResponseDto(initialSurvey);
    }

    private List<InitialSurveyQuestionResponseDto> mapToInitialSurveyResponseDto(InitialSurvey initialSurvey) {
        return initialSurvey.getQuestions().stream()
                .map(this::mapToInitialSurveyQuestionResponseDto)
                .collect(Collectors.toList());
    }

    private InitialSurveyQuestionResponseDto mapToInitialSurveyQuestionResponseDto(InitialSurveyQuestion question) {
        InitialSurveyQuestionResponseDto questionDto = modelMapper.map(question, InitialSurveyQuestionResponseDto.class);
        questionDto.setOptions(
                question.getOptions().stream()
                        .map(this::mapToInitialSurveyOptionResponseDto)
                        .collect(Collectors.toList()));
        return questionDto;
    }

    private InitialSurveyOptionResponseDto mapToInitialSurveyOptionResponseDto(InitialSurveyOption option) {
        return modelMapper.map(option, InitialSurveyOptionResponseDto.class);
    }

    private InitialSurvey mapToInitialSurvey(List<CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList) {
        InitialSurvey initialSurvey = new InitialSurvey();
        List<InitialSurveyQuestion> questions = createInitialSurveyQuestionDtoList.stream()
                .map(this::mapToSurveyQuestion)
                .peek(question -> question.setInitialSurvey(initialSurvey))
                .collect(Collectors.toList());
        initialSurvey.setQuestions(questions);
        return initialSurvey;
    }

    private InitialSurveyQuestion mapToSurveyQuestion(CreateInitialSurveyQuestionDto questionDto) {
        InitialSurveyQuestion question = new InitialSurveyQuestion();
        question.setOrder(questionDto.getOrder());
        question.setContent(questionDto.getContent());

        List<InitialSurveyOption> options = mapToSurveyOptions(questionDto.getOptions(), question);
        question.setOptions(options);
        return question;
    }

    private List<InitialSurveyOption> mapToSurveyOptions(List<CreateInitialSurveyOptionDto> optionDtos, InitialSurveyQuestion question) {
        return optionDtos.stream()
                .map(optionDto -> mapToSurveyOption(optionDto, question))
                .collect(Collectors.toList());
    }

    private InitialSurveyOption mapToSurveyOption(CreateInitialSurveyOptionDto optionDto, InitialSurveyQuestion question) {
        InitialSurveyOption option = new InitialSurveyOption();
        option.setOrder(optionDto.getOrder());
        option.setContent(optionDto.getContent());
        option.setQuestion(question);
        return option;
    }
}
