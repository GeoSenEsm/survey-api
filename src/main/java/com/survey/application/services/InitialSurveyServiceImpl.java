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
    public InitialSurveyResponseDto createInitialSurvey(CreateInitialSurveyDto createInitialSurveyDto) {
        InitialSurvey initialSurvey = mapToInitialSurvey(createInitialSurveyDto);
        InitialSurvey dbInitialSurvey = initialSurveyRepository.saveAndFlush(initialSurvey);
        entityManager.refresh(dbInitialSurvey);
        return modelMapper.map(dbInitialSurvey, InitialSurveyResponseDto.class);
    }


    @Override
    @Transactional
    public InitialSurveyResponseDto getInitialSurvey() {
        String queryStr = "SELECT i FROM InitialSurvey i";
        InitialSurvey initialSurvey = entityManager.createQuery(queryStr, InitialSurvey.class)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No initial survey created"));

        return mapToInitialSurveyResponseDto(initialSurvey);
    }

    private InitialSurveyResponseDto mapToInitialSurveyResponseDto(InitialSurvey survey) {
        InitialSurveyResponseDto initialSurveyResponseDto = modelMapper.map(survey, InitialSurveyResponseDto.class);
        initialSurveyResponseDto.setQuestions(
                survey.getQuestions().stream()
                        .map(this::mapToInitialSurveyQuestionResponseDto)
                        .collect(Collectors.toList())
        );
        return initialSurveyResponseDto;
    }

    private InitialSurveyQuestionResponseDto mapToInitialSurveyQuestionResponseDto(InitialSurveyQuestion question) {
        InitialSurveyQuestionResponseDto questionDto = modelMapper.map(question, InitialSurveyQuestionResponseDto.class);
        questionDto.setOptions(
                question.getOptions().stream()
                        .map(this::mapToInitialSurveyOptionResponseDto)
                        .collect(Collectors.toList())
        );
        return questionDto;
    }

    private InitialSurveyOptionResponseDto mapToInitialSurveyOptionResponseDto(InitialSurveyOption option) {
        return modelMapper.map(option, InitialSurveyOptionResponseDto.class);
    }


    private InitialSurvey mapToInitialSurvey(CreateInitialSurveyDto createInitialSurveyDto) {
        InitialSurvey initialSurvey = new InitialSurvey();
        List<InitialSurveyQuestion> questions = createInitialSurveyDto.getQuestions().stream()
                .map(dto -> mapToSurveyQuestion(dto, initialSurvey))
                .collect(Collectors.toList());
        initialSurvey.setQuestions(questions);
        return initialSurvey;
    }

    private InitialSurveyQuestion mapToSurveyQuestion(CreateInitialSurveyQuestionDto questionDto, InitialSurvey initialSurvey) {
        InitialSurveyQuestion question = new InitialSurveyQuestion();
        question.setOrder(questionDto.getOrder());
        question.setContent(questionDto.getContent());
        question.setInitialSurvey(initialSurvey);

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
