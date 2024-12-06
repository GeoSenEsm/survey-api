package com.survey.application.services;

import com.survey.application.dtos.initialSurvey.CreateInitialSurveyOptionDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyOptionResponseDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.InitialSurvey;
import com.survey.domain.models.InitialSurveyOption;
import com.survey.domain.models.InitialSurveyQuestion;
import com.survey.domain.repository.InitialSurveyRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SurveyResponsesServiceImpl.class)
class InitialSurveyServiceTest {
    private static final String QUESTION_CONTENT = "What is your favorite color?";
    private static final int QUESTION_ORDER = 1;
    private static final String OPTION_CONTENT = "Red";
    private static final int OPTION_ORDER = 1;
    @Mock
    private InitialSurveyRepository initialSurveyRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ClaimsPrincipalService claimsPrincipalService;
    @InjectMocks
    private InitialSurveyServiceImpl initialSurveyService;
    private List<CreateInitialSurveyQuestionDto> createInitialSurvey;
    private InitialSurvey initialSurvey;
    private InitialSurveyQuestionResponseDto responseDto;
    private InitialSurveyOptionResponseDto optionResponseDto;

    @BeforeEach
    void setUp() {
        createInitialSurvey = createCreateInitialSurvey();
        initialSurvey = createInitialSurvey();
        optionResponseDto = createOptionResponseDto();
        responseDto = createResponseDto(optionResponseDto);
    }

    @Test
    void createInitialSurvey_ShouldReturnListOfInitialSurveyQuestionResponseDto() {
        when(initialSurveyRepository.saveAndFlush(any(InitialSurvey.class))).thenReturn(initialSurvey);
        when(modelMapper.map(any(InitialSurveyQuestion.class), any()))
                .thenReturn(responseDto);
        when(modelMapper.map(any(InitialSurveyOption.class), any()))
                .thenReturn(optionResponseDto);

        List<InitialSurveyQuestionResponseDto> responseDtoList = initialSurveyService.createInitialSurvey(createInitialSurvey);

        verify(initialSurveyRepository).saveAndFlush(any(InitialSurvey.class));
        verify(entityManager).refresh(any(InitialSurvey.class));

        assertNotNull(responseDtoList);
        assertEquals(1, responseDtoList.size());
        assertEquals(QUESTION_CONTENT, responseDtoList.get(0).getContent());
        assertEquals(OPTION_CONTENT, responseDtoList.get(0).getOptions().get(0).getContent());
        assertEquals(QUESTION_ORDER, responseDtoList.get(0).getOrder());
        assertEquals(OPTION_ORDER, responseDtoList.get(0).getOptions().get(0).getOrder());
    }

    @Test
    void getInitialSurvey_ShouldThrowNoSuchElementException_WhenNoSurveyExists() {
        IdentityUser mockedIdentityUser = mock(IdentityUser.class);
        when(mockedIdentityUser.getRole()).thenReturn("Respondent");
        when(claimsPrincipalService.findIdentityUser()).thenReturn(mockedIdentityUser);
        when(initialSurveyRepository.findTopByRowVersionDesc()).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> initialSurveyService.getInitialSurvey()
        );

        assertEquals("Initial survey not published yet.", exception.getMessage());
    }

    private InitialSurveyOptionResponseDto createOptionResponseDto() {
        InitialSurveyOptionResponseDto optionResponseDto = new InitialSurveyOptionResponseDto();
        optionResponseDto.setOrder(OPTION_ORDER);
        optionResponseDto.setContent(OPTION_CONTENT);
        return optionResponseDto;
    }
    private InitialSurveyQuestionResponseDto createResponseDto(InitialSurveyOptionResponseDto optionResponseDto) {
        InitialSurveyQuestionResponseDto responseDto = new InitialSurveyQuestionResponseDto();
        responseDto.setOrder(QUESTION_ORDER);
        responseDto.setContent(QUESTION_CONTENT);
        responseDto.setOptions(List.of(optionResponseDto));
        return responseDto;
    }
    private InitialSurvey createInitialSurvey() {
        InitialSurvey initialSurvey = new InitialSurvey();
        
        InitialSurveyOption option = new InitialSurveyOption();
        option.setOrder(OPTION_ORDER);
        option.setContent(OPTION_CONTENT);
        
        InitialSurveyQuestion surveyQuestion = new InitialSurveyQuestion();
        surveyQuestion.setOrder(QUESTION_ORDER);
        surveyQuestion.setContent(QUESTION_CONTENT);
        surveyQuestion.setOptions(List.of(option));

        initialSurvey.setQuestions(List.of(surveyQuestion));
        return initialSurvey;
    }
    private List<CreateInitialSurveyQuestionDto> createCreateInitialSurvey() {
        CreateInitialSurveyOptionDto optionDto = new CreateInitialSurveyOptionDto();
        optionDto.setOrder(OPTION_ORDER);
        optionDto.setContent(OPTION_CONTENT);
        
        CreateInitialSurveyQuestionDto questionDto = new CreateInitialSurveyQuestionDto();
        questionDto.setOrder(QUESTION_ORDER);
        questionDto.setContent(QUESTION_CONTENT);
        questionDto.setOptions(List.of(optionDto));
        return List.of(questionDto);
    }
}
