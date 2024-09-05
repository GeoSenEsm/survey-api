package com.survey.api.integration;

import com.survey.application.dtos.QualityOfSleepDto;
import com.survey.domain.models.QualityOfSleep;
import com.survey.domain.repository.QualityOfSleepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class QualityOfSleepControllerTest {
    private final WebTestClient webTestClient;
    private final QualityOfSleepRepository repository;

    @Autowired
    public QualityOfSleepControllerTest(WebTestClient webTestClient, QualityOfSleepRepository repository) {
        this.webTestClient = webTestClient;
        this.repository = repository;
    }

    @Test
    void getForPlLangGivenInHeaderShouldReturnAllWithPolishDisplay(){
        var categories = webTestClient.get().uri("/api/qualityofsleep")
                .header("Accept-Lang", "pl")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<QualityOfSleepDto>>() {})
                .returnResult()
                .getResponseBody();

        var dbCategories = repository.findAll().stream().collect(Collectors.toMap(QualityOfSleep::getId, x -> x));

        assert categories != null;
        assertEquals(categories.size(), dbCategories.size());

        for (var category : categories){
            assertTrue(dbCategories.containsKey(category.getId()));
            var dbCategory = dbCategories.get(category.getId());
            assert dbCategory.getPolishDisplay().equals(category.getDisplay());
        }
    }

    @ParameterizedTest
    @MethodSource("getEnAndUnknownLangs")
    void getForUnknownOrNotGivenOrEnLangShouldReturnEnglishDisplays(String lang){
        var categories = webTestClient.get().uri("/api/qualityofsleep")
                .header("Accept-Lang", lang)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<QualityOfSleepDto>>() {})
                .returnResult()
                .getResponseBody();

        var dbCategories = repository.findAll().stream().collect(Collectors.toMap(QualityOfSleep::getId, x -> x));

        assert categories != null;
        assertEquals(categories.size(), dbCategories.size());

        for (var category : categories){
            assertTrue(dbCategories.containsKey(category.getId()));
            var dbCategory = dbCategories.get(category.getId());
            assert dbCategory.getEnglishDisplay().equals(category.getDisplay());
        }
    }

    public static Stream<Arguments> getEnAndUnknownLangs(){
        return Stream.of(
                Arguments.of("en"),
                Arguments.of("de"),
                Arguments.of("ch")
        );
    }

    @Test
    void getForNoLangGivenInHeaderShouldReturnAllWithEnglishDisplay(){
        var categories = webTestClient.get().uri("/api/qualityofsleep")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<QualityOfSleepDto>>() {})
                .returnResult()
                .getResponseBody();

        var dbCategories = repository.findAll().stream().collect(Collectors.toMap(QualityOfSleep::getId, x -> x));

        assert categories != null;
        assertEquals(categories.size(), dbCategories.size());

        for (var category : categories){
            assertTrue(dbCategories.containsKey(category.getId()));
            var dbCategory = dbCategories.get(category.getId());
            assert dbCategory.getEnglishDisplay().equals(category.getDisplay());
        }
    }
}
