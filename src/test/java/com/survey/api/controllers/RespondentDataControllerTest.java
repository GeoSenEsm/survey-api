package com.survey.api.controllers;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.services.RespondentDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RespondentDataControllerTest {

    @InjectMocks
    private RespondentDataController respondentDataController;

    @Mock
    private RespondentDataService respondentDataService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(respondentDataController).build();
    }

    @Test
    void createRespondent_ShouldReturnCreatedResponse() throws Exception {
        CreateRespondentDataDto dto = new CreateRespondentDataDto();

        Map<String, Object> responseMap = createResponseMap();
        when(respondentDataService.createRespondent(anyList(), anyString()))
                .thenReturn(responseMap);

        webTestClient.post()
                .uri("/api/respondents")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .bodyValue(Collections.singletonList(dto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .consumeWith(response -> {
                    Map body = response.getResponseBody();
                    assert body != null;
                    assert body.get("username").equals("User1");
                    assert body.get("id").equals(1);
                });

        verify(respondentDataService, times(1)).createRespondent(anyList(), anyString());
    }
    @Test
    void getAll_ShouldReturnOkResponse() {
        Map<String, Object> responseItem = createResponseMap();
        when(respondentDataService.getAll())
                .thenReturn(Collections.singletonList(responseItem));

        webTestClient.get()
                .uri("/api/respondents/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<Map<String, Object>>() {})
                .hasSize(1)
                .consumeWith(response -> {
                    List<Map<String, Object>> body = response.getResponseBody();
                    assert body != null;
                    assert body.get(0).get("username").equals("User1");
                    assert body.get(0).get("id").equals(1);
                });

        verify(respondentDataService, times(1)).getAll();
    }
    @Test
    void getFromUserContext_ShouldReturnOkResponse() {
        Map<String, Object> responseItem = createResponseMap();
        when(respondentDataService.getFromUserContext())
                .thenReturn(responseItem);

        webTestClient.get()
                .uri("/api/respondents")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .consumeWith(response -> {
                    Map body = response.getResponseBody();
                    assert body != null;
                    assert body.get("username").equals("User1");
                    assert body.get("id").equals(1);
                });

        verify(respondentDataService, times(1)).getFromUserContext();
    }
    private Map<String, Object> createResponseMap() {
        return Map.of("id", 1, "username", "User1");
    }
}
