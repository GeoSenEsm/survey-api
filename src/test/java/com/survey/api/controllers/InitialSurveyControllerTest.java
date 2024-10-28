package com.survey.api.controllers;

import com.survey.application.dtos.initialSurvey.CreateInitialSurveyOptionDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyOptionResponseDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.application.services.InitialSurveyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class InitialSurveyControllerTest {

    @InjectMocks
    private InitialSurveyController initialSurveyController;

    @Mock
    private InitialSurveyService initialSurveyService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(initialSurveyController).build();
    }

    @Test
    void createInitialSurvey_ShouldReturnCreatedResponse() {
        CreateInitialSurveyQuestionDto questionDto = createQuestionDto();
        InitialSurveyQuestionResponseDto responseDto = createQuestionResponseDto();

        when(initialSurveyService.createInitialSurvey(any()))
                .thenReturn(Collections.singletonList(responseDto));

        webTestClient.post()
                .uri("/api/initialsurvey")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Collections.singletonList(questionDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .hasSize(1)
                .consumeWith(response -> {
                    List<InitialSurveyQuestionResponseDto> body = response.getResponseBody();
                    assert body != null;
                    assert body.get(0).getContent().equals("Question1");
                    assert body.get(0).getOrder().equals(1);
                    assert body.get(0).getOptions().get(0).getContent().equals("Option1");
                    assert body.get(0).getOptions().get(0).getOrder().equals(1);
                });

        verify(initialSurveyService, times(1)).createInitialSurvey(anyList());
    }
    @Test
    void getInitialSurveyById_ShouldReturnOkResponse() {
        InitialSurveyQuestionResponseDto responseDto = createQuestionResponseDto();

        when(initialSurveyService.getInitialSurvey())
                .thenReturn(Collections.singletonList(responseDto));

        webTestClient.get()
                .uri("/api/initialsurvey")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InitialSurveyQuestionResponseDto.class)
                .hasSize(1)
                .consumeWith(response -> {
                    List<InitialSurveyQuestionResponseDto> body = response.getResponseBody();
                    assert body != null;
                    assert body.get(0).getContent().equals("Question1");
                    assert body.get(0).getOrder().equals(1);
                    assert body.get(0).getOptions().get(0).getContent().equals("Option1");
                    assert body.get(0).getOptions().get(0).getOrder().equals(1);
                });

        verify(initialSurveyService, times(1)).getInitialSurvey();
    }
    private InitialSurveyQuestionResponseDto createQuestionResponseDto() {
        InitialSurveyQuestionResponseDto responseDto = new InitialSurveyQuestionResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setContent("Question1");
        responseDto.setOrder(1);
        responseDto.setOptions(List.of(createOptionResponseDto()));
        return responseDto;
    }

    private InitialSurveyOptionResponseDto createOptionResponseDto() {
        InitialSurveyOptionResponseDto optionResponseDto = new InitialSurveyOptionResponseDto();
        optionResponseDto.setId(UUID.randomUUID());
        optionResponseDto.setOrder(1);
        optionResponseDto.setContent("Option1");
        return optionResponseDto;
    }

    private CreateInitialSurveyQuestionDto createQuestionDto() {
        CreateInitialSurveyOptionDto optionDto = new CreateInitialSurveyOptionDto();
        optionDto.setContent("Option1");
        optionDto.setOrder(1);

        CreateInitialSurveyQuestionDto questionDto = new CreateInitialSurveyQuestionDto();
        questionDto.setContent("Question1");
        questionDto.setOrder(1);
        questionDto.setOptions(List.of(optionDto));
        return questionDto;
    }
}
