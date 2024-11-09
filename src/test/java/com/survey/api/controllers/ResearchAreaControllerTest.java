package com.survey.api.controllers;

import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;
import com.survey.application.services.ResearchAreaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ResearchAreaControllerTest {
    private static final BigDecimal VALID_LATITUDE = new BigDecimal("52.237049");
    private static final BigDecimal VALID_LONGITUDE = new BigDecimal("21.017532");
    @InjectMocks
    private ResearchAreaController researchAreaController;
    @Mock
    private ResearchAreaService researchAreaService;
    private WebTestClient webTestClient;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(researchAreaController).build();
    }
    @Test
    void saveResearchAreaData_ShouldReturnCreatedStatus() {
        ResearchAreaDto researchAreaDto = new ResearchAreaDto();
        researchAreaDto.setLatitude(VALID_LATITUDE);
        researchAreaDto.setLongitude(VALID_LONGITUDE);

        ResponseResearchAreaDto responseDto = new ResponseResearchAreaDto();
        responseDto.setLatitude(VALID_LATITUDE);
        responseDto.setLongitude(VALID_LONGITUDE);

        when(researchAreaService.saveResearchArea(any(ResearchAreaDto.class))).thenReturn(responseDto);

        webTestClient.post()
                .uri("/api/researcharea")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(researchAreaDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ResponseResearchAreaDto.class)
                .value(result -> {
                    assertEquals(VALID_LATITUDE, result.getLatitude());
                    assertEquals(VALID_LONGITUDE, result.getLongitude());
                });
    }

    @Test
    void getResearchArea_ShouldReturnResearchAreaWhenExists() {
        ResponseResearchAreaDto responseDto = new ResponseResearchAreaDto();
        responseDto.setLatitude(VALID_LATITUDE);
        responseDto.setLongitude(VALID_LONGITUDE);

        when(researchAreaService.getResearchArea()).thenReturn(responseDto);

        webTestClient.get()
                .uri("/api/researcharea")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseResearchAreaDto.class)
                .value(result -> {
                    assertEquals(VALID_LATITUDE, result.getLatitude());
                    assertEquals(VALID_LONGITUDE, result.getLongitude());
                });
    }

    @Test
    void getResearchArea_ShouldReturnNotFoundWhenNoResearchAreaExists() {
        when(researchAreaService.getResearchArea()).thenReturn(null);

        webTestClient.get()
                .uri("/api/researcharea")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteResearchArea_ShouldReturnOkStatusWhenDeleted() {
        when(researchAreaService.deleteResearchArea()).thenReturn(true);

        webTestClient.delete()
                .uri("/api/researcharea")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deleteResearchArea_ShouldReturnNotFoundWhenNoResearchAreaExists() {
        when(researchAreaService.deleteResearchArea()).thenReturn(false);

        webTestClient.delete()
                .uri("/api/researcharea")
                .exchange()
                .expectStatus().isNotFound();
    }
}