package com.survey.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.surveyDtos.*;
import com.survey.application.services.StorageService;
import com.survey.application.services.SurveyService;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class SurveyControllerTest {
    @InjectMocks
    SurveyController surveyController;
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private SurveyService surveyService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private StorageService storageService;
    private ResponseSurveyDto responseSurveyDto;
    private CreateSurveyDto createSurveyDto;
    private static final String SURVEY_NAME = "Survey1";
    private static final String SECTION_NAME = "Section1";
    private static final String QUESTION_CONTENT = "What is your favorite color?";
    private static final String OPTION_LABEL = "Red";
    private static final int SECTION_ORDER = 1;
    private static final int QUESTION_ORDER = 1;
    private static final int OPTION_ORDER = 1;
    private static final String IMAGE_PATH = "uploads/Survey1/questions/1/options/1";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(surveyController).build();
        createSurveyDto = createMockCreateSurveyDto();
        responseSurveyDto = createMockResponseSurveyDto();
    }

    @Test
    void createSurvey_ShouldReturnCreatedResponse() throws Exception {
        String createSurveyDtoJson = new ObjectMapper().writeValueAsString(createSurveyDto);
        MockMultipartFile file = new MockMultipartFile("files", "testFile.txt", "text/plain", "sample file content".getBytes());

        when(surveyService.createSurvey(any(CreateSurveyDto.class), any()))
                .thenReturn(responseSurveyDto);
        when(objectMapper.readValue(anyString(), eq(CreateSurveyDto.class))).thenReturn(createSurveyDto);
        when(storageService.store(any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(IMAGE_PATH);

        var mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/surveys")
                        .file(file)
                        .param("json", createSurveyDtoJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ResponseSurveyDto actualResponse = new ObjectMapper().readValue(jsonResponse, ResponseSurveyDto.class);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(responseSurveyDto);
    }

    private CreateSurveyDto createMockCreateSurveyDto() {
        CreateOptionDto createOptionDto = new CreateOptionDto();
        createOptionDto.setLabel(OPTION_LABEL);
        createOptionDto.setOrder(OPTION_ORDER);

        CreateQuestionDto createQuestionDto = new CreateQuestionDto();
        createQuestionDto.setQuestionType(QuestionType.image_choice.name());
        createQuestionDto.setOrder(QUESTION_ORDER);
        createQuestionDto.setContent(QUESTION_CONTENT);
        createQuestionDto.setOptions(List.of(createOptionDto));

        CreateSurveySectionDto createSurveySectionDto = new CreateSurveySectionDto();
        createSurveySectionDto.setName(SECTION_NAME);
        createSurveySectionDto.setOrder(SECTION_ORDER);
        createSurveySectionDto.setDisplayOnOneScreen(true);
        createSurveySectionDto.setVisibility(Visibility.always.name());
        createSurveySectionDto.setQuestions(List.of(createQuestionDto));

        CreateSurveyDto createSurveyDto = new CreateSurveyDto();
        createSurveyDto.setName(SURVEY_NAME);
        createSurveyDto.setSections(List.of(createSurveySectionDto));
        return createSurveyDto;
    }

    private ResponseSurveyDto createMockResponseSurveyDto() {
        ResponseOptionDto responseOptionDto = new ResponseOptionDto();
        responseOptionDto.setOrder(OPTION_ORDER);
        responseOptionDto.setLabel(OPTION_LABEL);
        responseOptionDto.setImagePath(IMAGE_PATH);

        ResponseQuestionDto responseQuestionDto = new ResponseQuestionDto();
        responseQuestionDto.setContent(QUESTION_CONTENT);
        responseQuestionDto.setQuestionType(QuestionType.image_choice);
        responseQuestionDto.setOrder(QUESTION_ORDER);
        responseQuestionDto.setOptions(List.of(responseOptionDto));

        ResponseSurveySectionDto responseSurveySectionDto = new ResponseSurveySectionDto();
        responseSurveySectionDto.setName(SECTION_NAME);
        responseSurveySectionDto.setVisibility(Visibility.always);
        responseSurveySectionDto.setDisplayOnOneScreen(true);
        responseSurveySectionDto.setQuestions(List.of(responseQuestionDto));

        ResponseSurveyDto responseSurveyDto = new ResponseSurveyDto();
        responseSurveyDto.setName(SURVEY_NAME);
        responseSurveyDto.setSections(List.of(responseSurveySectionDto));
        return responseSurveyDto;
    }
}
