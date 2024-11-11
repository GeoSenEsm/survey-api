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
import java.util.List;

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
        ResearchAreaDto researchAreaDto1 = new ResearchAreaDto();
        researchAreaDto1.setLatitude(VALID_LATITUDE);
        researchAreaDto1.setLongitude(VALID_LONGITUDE);

        ResearchAreaDto researchAreaDto2 = new ResearchAreaDto();
        researchAreaDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        researchAreaDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));

        ResponseResearchAreaDto responseDto1 = new ResponseResearchAreaDto();
        responseDto1.setLatitude(VALID_LATITUDE);
        responseDto1.setLongitude(VALID_LONGITUDE);

        ResponseResearchAreaDto responseDto2 = new ResponseResearchAreaDto();
        responseDto2.setLatitude(VALID_LATITUDE.add(BigDecimal.ONE));
        responseDto2.setLongitude(VALID_LONGITUDE.add(BigDecimal.ONE));

        when(researchAreaService.saveResearchArea(any(List.class))).thenReturn(List.of(responseDto1, responseDto2));

        webTestClient.post()
                .uri("/api/researcharea")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(researchAreaDto1, researchAreaDto2))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(ResponseResearchAreaDto.class)
                .value(result -> {
                    assertEquals(VALID_LATITUDE, result.get(0).getLatitude());
                    assertEquals(VALID_LONGITUDE, result.get(0).getLongitude());
                    assertEquals(VALID_LATITUDE.add(BigDecimal.ONE), result.get(1).getLatitude());
                    assertEquals(VALID_LONGITUDE.add(BigDecimal.ONE), result.get(1).getLongitude());
                });
    }

    @Test
    void getResearchArea_ShouldReturnResearchAreaWhenExists() {
        ResponseResearchAreaDto responseDto = new ResponseResearchAreaDto();
        responseDto.setLatitude(VALID_LATITUDE);
        responseDto.setLongitude(VALID_LONGITUDE);

        when(researchAreaService.getResearchArea()).thenReturn(List.of(responseDto));

        webTestClient.get()
                .uri("/api/researcharea")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseResearchAreaDto.class)
                .value(result -> {
                    assertEquals(VALID_LATITUDE, result.get(0).getLatitude());
                    assertEquals(VALID_LONGITUDE, result.get(0).getLongitude());
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