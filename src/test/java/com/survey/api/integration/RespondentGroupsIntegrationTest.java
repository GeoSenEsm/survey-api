package com.survey.api.integration;

import com.survey.application.dtos.RespondentGroupDto;
import com.survey.domain.models.RespondentGroup;
import com.survey.domain.repository.RespondentGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class RespondentGroupsIntegrationTest {
    private final WebTestClient webTestClient;
    private final RespondentGroupRepository repository;

    @Autowired
    public RespondentGroupsIntegrationTest(WebTestClient webTestClient, RespondentGroupRepository repository) {
        this.webTestClient = webTestClient;
        this.repository = repository;
    }

    @Test
    void getRespondentGroups_ShouldReturnAllGroups_WhenNoRespondentIdIsProvide(){
        var categories = webTestClient.get().uri("/api/respondentgroups")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<RespondentGroupDto>>() {})
                .returnResult()
                .getResponseBody();

        var dbCategories = repository.findAll().stream().collect(Collectors.toMap(RespondentGroup::getId, x -> x));

        assert categories != null;
        assertEquals(categories.size(), dbCategories.size());

        for (var category : categories){
            assertTrue(dbCategories.containsKey(category.getId()));
            var dbCategory = dbCategories.get(category.getId());
            assert dbCategory.getName().equals(category.getName());
        }
    }
}
