package com.survey.api.controllers;

import com.survey.application.dtos.surveyDtos.*;
import com.survey.application.services.SurveyService;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


public class SurveyControllerTest {
    @InjectMocks
    SurveyController surveyController;
    @Mock
    private SurveyService surveyService;
    private WebTestClient webTestClient;
    private CreateSurveyDto createSurveyDto;
    private static final String SURVEY_NAME = "Survey1";
    private static final String SECTION_NAME = "Section1";
    private static final String QUESTION_CONTENT = "What is your favorite color?";
    private static final String OPTION_LABEL_1 = "Red";
    private static final String OPTION_LABEL_2 = "Blue";
    private static final int SECTION_ORDER = 1;
    private static final int QUESTION_ORDER = 1;
    private static final int OPTION_ORDER_1 = 1;
    private static final int OPTION_ORDER_2 = 2;
    private static final UUID SURVEY_ID = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(surveyController).build();
        createSurveyDto = createMockCreateSurveyDto();
    }

    @Test
    void createSurvey_ShouldReturnCreatedResponse() {
        ResponseSurveyDto responseSurveyDto = createMockResponseSurveyDto();
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("json", createSurveyDto, MediaType.APPLICATION_JSON);

        when(surveyService.createSurvey(any(), any()))
                .thenReturn(responseSurveyDto);

        ResponseSurveyDto response = webTestClient.post()
                .uri("/api/surveys")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ResponseSurveyDto.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getName()).isEqualTo(SURVEY_NAME);
        Assertions.assertThat(response.getSections().get(0).getQuestions().get(0).getContent()).isEqualTo(QUESTION_CONTENT);
        Assertions.assertThat(response.getSections().get(0).getQuestions().get(0).getOptions().get(0).getLabel()).isEqualTo(OPTION_LABEL_1);
    }
    @Test
    void deleteSurvey_ShouldReturnOk() {
        doNothing().when(surveyService).deleteSurvey(SURVEY_ID);

        webTestClient.delete()
                .uri("/api/surveys/" + SURVEY_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateSurvey_ShouldReturnUpdatedSurvey() {
        ResponseSurveyDto responseSurveyDto = createMockResponseSurveyDto();

        when(surveyService.updateSurvey(eq(SURVEY_ID), any(), any())).thenReturn(responseSurveyDto);
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("json", createSurveyDto, MediaType.APPLICATION_JSON);

        ResponseSurveyDto response = webTestClient.put()
                .uri("/api/surveys/" + SURVEY_ID)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseSurveyDto.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getName()).isEqualTo(SURVEY_NAME);
        Assertions.assertThat(response.getSections().get(0).getQuestions().get(0).getContent()).isEqualTo(QUESTION_CONTENT);
        Assertions.assertThat(response.getSections().get(0).getQuestions().get(0).getOptions().get(0).getLabel()).isEqualTo(OPTION_LABEL_1);
    }
    @Test
    void publishSurvey_ShouldReturnNoContent() {
        doNothing().when(surveyService).publishSurvey(SURVEY_ID);

        webTestClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/api/surveys/publish").queryParam("surveyId", SURVEY_ID).build())
                .exchange()
                .expectStatus().isNoContent();
    }

    private CreateSurveyDto createMockCreateSurveyDto() {
        CreateOptionDto createOptionDto1 = new CreateOptionDto();
        createOptionDto1.setLabel(OPTION_LABEL_1);
        createOptionDto1.setOrder(OPTION_ORDER_1);

        CreateOptionDto createOptionDto2 = new CreateOptionDto();
        createOptionDto2.setLabel(OPTION_LABEL_2);
        createOptionDto2.setOrder(OPTION_ORDER_2);

        CreateQuestionDto createQuestionDto = new CreateQuestionDto();
        createQuestionDto.setQuestionType(QuestionType.single_choice.name());
        createQuestionDto.setOrder(QUESTION_ORDER);
        createQuestionDto.setContent(QUESTION_CONTENT);
        createQuestionDto.setOptions(List.of(createOptionDto1, createOptionDto2));

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
        responseOptionDto.setOrder(OPTION_ORDER_1);
        responseOptionDto.setLabel(OPTION_LABEL_1);

        ResponseQuestionDto responseQuestionDto = new ResponseQuestionDto();
        responseQuestionDto.setContent(QUESTION_CONTENT);
        responseQuestionDto.setQuestionType(QuestionType.single_choice);
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
