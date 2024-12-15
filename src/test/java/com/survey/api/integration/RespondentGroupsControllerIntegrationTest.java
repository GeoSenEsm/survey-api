package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.RespondentGroupDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentGroup;
import com.survey.domain.repository.RespondentGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class RespondentGroupsControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final RespondentGroupRepository repository;
    private final TestUtils testUtils;

    private static final String ADMIN_PASSWORD = "testAdminPassword";
    private static final String RESPONDENT_PASSWORD = "testRespondentPassword";

    @Autowired
    public RespondentGroupsControllerIntegrationTest(WebTestClient webTestClient, RespondentGroupRepository repository, TestUtils testUtils) {
        this.webTestClient = webTestClient;
        this.repository = repository;
        this.testUtils = testUtils;
    }

    @Test
    void getRespondentGroupsAsAdmin_ShouldReturnAllGroups_WhenNoRespondentIdIsProvide(){
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        var categories = webTestClient.get()
                .uri("/api/respondentgroups")
                .header("Authorization", "Bearer " + adminToken)
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

    @Test
    void getRespondentGroupsAsRespondent_ShouldReturnAllGroups_WhenNoRespondentIdIsProvide(){
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), RESPONDENT_PASSWORD);
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, RESPONDENT_PASSWORD);

        var categories = webTestClient.get()
                .uri("/api/respondentgroups")
                .header("Authorization", "Bearer " + respondentToken)
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
