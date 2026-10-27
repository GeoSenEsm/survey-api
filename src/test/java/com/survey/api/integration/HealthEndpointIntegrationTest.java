package com.survey.api.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "management.health.mongo.enabled=false")
@AutoConfigureWebTestClient
public class HealthEndpointIntegrationTest {
    private final WebTestClient webTestClient;

    @Autowired
    public HealthEndpointIntegrationTest(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    public void healthIsAvailableWithoutAuthentication() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    public void otherActuatorEndpointsStillRequireAuthentication() {
        webTestClient.get().uri("/actuator")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
