package com.survey.api.integration;

import com.survey.application.dtos.AgeCategoryDto;
import com.survey.domain.models.AgeCategory;
import com.survey.domain.repository.AgeCategoryRepository;
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
public class AgeCategoryControllerTest {
    private final WebTestClient webTestClient;
    private final AgeCategoryRepository ageCategoryRepository;

    @Autowired
    public AgeCategoryControllerTest(WebTestClient webTestClient, AgeCategoryRepository ageCategoryRepository) {
        this.webTestClient = webTestClient;
        this.ageCategoryRepository = ageCategoryRepository;
    }

    @Test
    void getForPlLangGivenInHeaderShouldReturnAllAgeCategoriesWithPolishDisplay(){
        var categories = webTestClient.get().uri("/api/agecategories")
                .header("Accept-Lang", "pl")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<AgeCategoryDto>>() {})
                .returnResult()
                .getResponseBody();

        var dbCategories = ageCategoryRepository.findAll().stream().collect(Collectors.toMap(AgeCategory::getId, x -> x));

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
        var categories = webTestClient.get().uri("/api/agecategories")
                .header("Accept-Lang", lang)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<AgeCategoryDto>>() {})
                .returnResult()
                .getResponseBody();

        var dbCategories = ageCategoryRepository.findAll().stream().collect(Collectors.toMap(AgeCategory::getId, x -> x));

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
    void getForNoLangGivenInHeaderShouldReturnAllAgeCategoriesWithEnglishDisplay(){
        var categories = webTestClient.get().uri("/api/agecategories")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<AgeCategoryDto>>() {})
                .returnResult()
                .getResponseBody();

        var dbCategories = ageCategoryRepository.findAll().stream().collect(Collectors.toMap(AgeCategory::getId, x -> x));

        assert categories != null;
        assertEquals(categories.size(), dbCategories.size());

        for (var category : categories){
            assertTrue(dbCategories.containsKey(category.getId()));
            var dbCategory = dbCategories.get(category.getId());
            assert dbCategory.getEnglishDisplay().equals(category.getDisplay());
        }
    }
}
