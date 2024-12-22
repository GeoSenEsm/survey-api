package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.surveyDtos.*;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
public class SurveyControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final IdentityUserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final TestUtils testUtils;

    private static final String SINGLE_CHOICE_QUESTION_CONTENT = "This is a single choice question.";
    private static final int SINGLE_CHOICE_QUESTION_ORDER = 1;
    private static final String SINGLE_CHOICE_QUESTION_OPTION_CONTENT_1 = "single option 1";
    private static final String SINGLE_CHOICE_QUESTION_OPTION_CONTENT_2 = "single option 2";

    private static final String TEXT_INPUT_QUESTION_CONTENT = "Write some text here.";
    private static final int TEXT_INPUT_QUESTION_ORDER = 2;

    private static final String YES_NO_QUESTION_CONTENT = "This is a yes/no question.";
    private static final int YES_NO_QUESTION_ORDER = 3;

    private static final String LINEAR_SCALE_QUESTION_CONTENT = "This is a linear scale question.";
    private static final int LINEAR_SCALE_QUESTION_ORDER = 4;
    private static final int LINEAR_SCALE_QUESTION_FROM = 1;
    private static final String LINEAR_SCALE_QUESTION_FROM_LABEL = "from label";
    private static final int LINEAR_SCALE_QUESTION_TO = 5;
    private static final String LINEAR_SCALE_QUESTION_TO_LABEL = "to label";

    private static final String NUMBER_INPUT_QUESTION_CONTENT = "This is a number input question.";
    private static final int NUMBER_INPUT_QUESTION_ORDER = 5;

    private static final String MULTIPLE_CHOICE_QUESTION_CONTENT = "This is a multiple choice question.";
    private static final int MULTIPLE_CHOICE_QUESTION_ORDER = 6;
    private static final String MULTIPLE_CHOICE_QUESTION_OPTION_CONTENT_1 = "multiple option 1";
    private static final String MULTIPLE_CHOICE_QUESTION_OPTION_CONTENT_2 = "multiple option 2";




    private static final String SURVEY_NAME = "Survey";
    private static final String SECTION_NAME = "Section1";
    private static final String ADMIN_PASSWORD = "testAdminPassword";

    @Autowired
    public SurveyControllerIntegrationTest(WebTestClient webTestClient,
                                           IdentityUserRepository userRepository,
                                           SurveyRepository surveyRepository,
                                           SurveyParticipationRepository surveyParticipationRepository, TestUtils testUtils) {
        this.webTestClient = webTestClient;
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.testUtils = testUtils;
    }
    @BeforeEach
    void SetUp(){
        surveyParticipationRepository.deleteAll();
        userRepository.deleteAll();
        surveyRepository.deleteAll();
    }
    @Test
    void createSurvey_ShouldBeOK() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        MultipartBodyBuilder multipartBodyBuilder = buildMultipartBodyFromDto(createSurveyDto);

        ResponseSurveyDto response = webTestClient.post()
                .uri("/api/surveys")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ResponseSurveyDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(SURVEY_NAME);

        assertThat(response.getSections()).hasSize(1);
        ResponseSurveySectionDto section = response.getSections().get(0);
        assertThat(section.getName()).isEqualTo(SECTION_NAME);

        List<ResponseQuestionDto> sortedQuestions = section.getQuestions().stream()
                .sorted(Comparator.comparingInt(ResponseQuestionDto::getOrder))
                .toList();

        assertThat(sortedQuestions).hasSize(6);

        ResponseQuestionDto singleChoiceQuestion = sortedQuestions.get(0);
        assertThat(singleChoiceQuestion.getOrder()).isEqualTo(SINGLE_CHOICE_QUESTION_ORDER);
        assertThat(singleChoiceQuestion.getContent()).isEqualTo(SINGLE_CHOICE_QUESTION_CONTENT);
        assertThat(singleChoiceQuestion.getQuestionType().toString()).isEqualTo(QuestionType.single_choice.name());
        List<ResponseOptionDto> singleChoiceOptions = singleChoiceQuestion.getOptions().stream()
                .sorted(Comparator.comparingInt(ResponseOptionDto::getOrder))
                .toList();
        assertThat(singleChoiceOptions).hasSize(2);
        assertThat(singleChoiceOptions.get(0).getLabel()).isEqualTo(SINGLE_CHOICE_QUESTION_OPTION_CONTENT_1);
        assertThat(singleChoiceOptions.get(1).getLabel()).isEqualTo(SINGLE_CHOICE_QUESTION_OPTION_CONTENT_2);

        ResponseQuestionDto textInputQuestion = sortedQuestions.get(1);
        assertThat(textInputQuestion.getOrder()).isEqualTo(TEXT_INPUT_QUESTION_ORDER);
        assertThat(textInputQuestion.getContent()).isEqualTo(TEXT_INPUT_QUESTION_CONTENT);
        assertThat(textInputQuestion.getQuestionType().toString()).isEqualTo(QuestionType.text_input.name());
        assertThat(textInputQuestion.getOptions()).isEmpty();

        ResponseQuestionDto yesNoQuestion = sortedQuestions.get(2);
        assertThat(yesNoQuestion.getOrder()).isEqualTo(YES_NO_QUESTION_ORDER);
        assertThat(yesNoQuestion.getContent()).isEqualTo(YES_NO_QUESTION_CONTENT);
        assertThat(yesNoQuestion.getQuestionType().toString()).isEqualTo(QuestionType.yes_no_choice.name());
        assertThat(yesNoQuestion.getOptions()).isEmpty();

        ResponseQuestionDto linearScaleQuestion = sortedQuestions.get(3);
        assertThat(linearScaleQuestion.getOrder()).isEqualTo(LINEAR_SCALE_QUESTION_ORDER);
        assertThat(linearScaleQuestion.getContent()).isEqualTo(LINEAR_SCALE_QUESTION_CONTENT);
        assertThat(linearScaleQuestion.getQuestionType().toString()).isEqualTo(QuestionType.linear_scale.name());
        assertThat(linearScaleQuestion.getNumberRange().getFrom()).isEqualTo(LINEAR_SCALE_QUESTION_FROM);
        assertThat(linearScaleQuestion.getNumberRange().getFromLabel()).isEqualTo(LINEAR_SCALE_QUESTION_FROM_LABEL);
        assertThat(linearScaleQuestion.getNumberRange().getTo()).isEqualTo(LINEAR_SCALE_QUESTION_TO);
        assertThat(linearScaleQuestion.getNumberRange().getToLabel()).isEqualTo(LINEAR_SCALE_QUESTION_TO_LABEL);

        ResponseQuestionDto numberInputQuestion = sortedQuestions.get(4);
        assertThat(numberInputQuestion.getOrder()).isEqualTo(NUMBER_INPUT_QUESTION_ORDER);
        assertThat(numberInputQuestion.getContent()).isEqualTo(NUMBER_INPUT_QUESTION_CONTENT);
        assertThat(numberInputQuestion.getQuestionType().toString()).isEqualTo(QuestionType.number_input.name());
        assertThat(numberInputQuestion.getOptions()).isEmpty();

        ResponseQuestionDto multipleChoiceQuestion = sortedQuestions.get(5);
        assertThat(multipleChoiceQuestion.getOrder()).isEqualTo(MULTIPLE_CHOICE_QUESTION_ORDER);
        assertThat(multipleChoiceQuestion.getContent()).isEqualTo(MULTIPLE_CHOICE_QUESTION_CONTENT);
        assertThat(multipleChoiceQuestion.getQuestionType().toString()).isEqualTo(QuestionType.single_choice.name());
        List<ResponseOptionDto> multipleChoiceOptions = multipleChoiceQuestion.getOptions().stream()
                .sorted(Comparator.comparingInt(ResponseOptionDto::getOrder))
                .toList();
        assertThat(multipleChoiceOptions).hasSize(2);
        assertThat(multipleChoiceOptions.get(0).getLabel()).isEqualTo(MULTIPLE_CHOICE_QUESTION_OPTION_CONTENT_1);
        assertThat(multipleChoiceOptions.get(1).getLabel()).isEqualTo(MULTIPLE_CHOICE_QUESTION_OPTION_CONTENT_2);
    }

    @Test
    void getSurvey_ShouldReturnOk() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/surveys")
                        .queryParam("surveyId", responseSurveyDto.getId().toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseSurveyDto.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getName()).isEqualTo(SURVEY_NAME);
    }

    @Test
    void updateSurvey_ShouldReturnOK() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        MultipartBodyBuilder multipartBodyBuilder = buildMultipartBodyFromDto(createSurveyDto);
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        webTestClient.put()
                .uri("/api/surveys/" + responseSurveyDto.getId().toString())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void updateSurvey_ShouldReturnBadRequest_WhenSurveyAlreadyPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        MultipartBodyBuilder multipartBodyBuilder = buildMultipartBodyFromDto(createSurveyDto);
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        webTestClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/api/surveys/publish")
                        .queryParam("surveyId", responseSurveyDto.getId().toString())
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isEqualTo(204);

        webTestClient.put()
                .uri("/api/surveys/" + responseSurveyDto.getId().toString())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void deleteSurvey_ShouldReturnOK() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        CreateSurveyDto createSurveyDto = createValidSurveyDto();
        ResponseSurveyDto responseSurveyDto = saveSurveyAsAdmin(createSurveyDto);

        webTestClient.delete()
                .uri("/api/surveys/" + responseSurveyDto.getId().toString())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void deleteSurvey_ShouldReturnNotFound_WhenNoSurveyWithThisId() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        webTestClient.delete()
                .uri("/api/surveys/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class).value(errorMessage -> assertThat(errorMessage).contains("Survey not found"));
    }

    private MultipartBodyBuilder buildMultipartBodyFromDto(CreateSurveyDto createSurveyDto) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("json", createSurveyDto, MediaType.APPLICATION_JSON);
        return multipartBodyBuilder;
    }

    private ResponseSurveyDto saveSurveyAsAdmin(CreateSurveyDto createSurveyDto) {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("json", createSurveyDto, MediaType.APPLICATION_JSON);

        return webTestClient.post()
                .uri("/api/surveys")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ResponseSurveyDto.class)
                .returnResult()
                .getResponseBody();
    }
    private CreateSurveyDto createValidSurveyDto(){
        CreateOptionDto createSingleOptionDto1 = new CreateOptionDto();
        createSingleOptionDto1.setLabel(SINGLE_CHOICE_QUESTION_OPTION_CONTENT_1);
        createSingleOptionDto1.setOrder(1);
        CreateOptionDto createSingleOptionDto2 = new CreateOptionDto();
        createSingleOptionDto2.setLabel(SINGLE_CHOICE_QUESTION_OPTION_CONTENT_2);
        createSingleOptionDto2.setOrder(2);
        CreateQuestionDto createSingleChoiceQuestionDto = new CreateQuestionDto();
        createSingleChoiceQuestionDto.setQuestionType(QuestionType.single_choice.name());
        createSingleChoiceQuestionDto.setOrder(SINGLE_CHOICE_QUESTION_ORDER);
        createSingleChoiceQuestionDto.setContent(SINGLE_CHOICE_QUESTION_CONTENT);
        createSingleChoiceQuestionDto.setOptions(List.of(createSingleOptionDto1, createSingleOptionDto2));


        CreateQuestionDto createTextInputQuestionDto = new CreateQuestionDto();
        createTextInputQuestionDto.setQuestionType(QuestionType.text_input.name());
        createTextInputQuestionDto.setOrder(TEXT_INPUT_QUESTION_ORDER);
        createTextInputQuestionDto.setContent(TEXT_INPUT_QUESTION_CONTENT);


        CreateQuestionDto createYesNoQuestionDto = new CreateQuestionDto();
        createYesNoQuestionDto.setQuestionType(QuestionType.yes_no_choice.name());
        createYesNoQuestionDto.setOrder(YES_NO_QUESTION_ORDER);
        createYesNoQuestionDto.setContent(YES_NO_QUESTION_CONTENT);


        CreateNumberRangeOptionDto createNumberRangeOptionDto = new CreateNumberRangeOptionDto();
        createNumberRangeOptionDto.setFrom(LINEAR_SCALE_QUESTION_FROM);
        createNumberRangeOptionDto.setFromLabel(LINEAR_SCALE_QUESTION_FROM_LABEL);
        createNumberRangeOptionDto.setTo(LINEAR_SCALE_QUESTION_TO);
        createNumberRangeOptionDto.setToLabel(LINEAR_SCALE_QUESTION_TO_LABEL);
        CreateQuestionDto createLinearScaleQuestionDto = new CreateQuestionDto();
        createLinearScaleQuestionDto.setQuestionType(QuestionType.linear_scale.name());
        createLinearScaleQuestionDto.setOrder(LINEAR_SCALE_QUESTION_ORDER);
        createLinearScaleQuestionDto.setContent(LINEAR_SCALE_QUESTION_CONTENT);
        createLinearScaleQuestionDto.setNumberRange(createNumberRangeOptionDto);


        CreateQuestionDto createNumberInputQuestionDto = new CreateQuestionDto();
        createNumberInputQuestionDto.setQuestionType(QuestionType.number_input.name());
        createNumberInputQuestionDto.setOrder(NUMBER_INPUT_QUESTION_ORDER);
        createNumberInputQuestionDto.setContent(NUMBER_INPUT_QUESTION_CONTENT);


        CreateOptionDto createMultipleOptionDto1 = new CreateOptionDto();
        createMultipleOptionDto1.setLabel(MULTIPLE_CHOICE_QUESTION_OPTION_CONTENT_1);
        createMultipleOptionDto1.setOrder(1);
        CreateOptionDto createMultipleOptionDto2 = new CreateOptionDto();
        createMultipleOptionDto2.setLabel(MULTIPLE_CHOICE_QUESTION_OPTION_CONTENT_2);
        createMultipleOptionDto2.setOrder(2);
        CreateQuestionDto createMultipleChoiceQuestionDto = new CreateQuestionDto();
        createMultipleChoiceQuestionDto.setQuestionType(QuestionType.single_choice.name());
        createMultipleChoiceQuestionDto.setOrder(MULTIPLE_CHOICE_QUESTION_ORDER);
        createMultipleChoiceQuestionDto.setContent(MULTIPLE_CHOICE_QUESTION_CONTENT);
        createMultipleChoiceQuestionDto.setOptions(List.of(createMultipleOptionDto1, createMultipleOptionDto2));


        CreateSurveySectionDto createSurveySectionDto = new CreateSurveySectionDto();
        createSurveySectionDto.setName(SECTION_NAME);
        createSurveySectionDto.setOrder(1);
        createSurveySectionDto.setDisplayOnOneScreen(true);
        createSurveySectionDto.setVisibility("always");
        createSurveySectionDto.setQuestions(List.of(
                createSingleChoiceQuestionDto,
                createTextInputQuestionDto,
                createYesNoQuestionDto,
                createLinearScaleQuestionDto,
                createNumberInputQuestionDto,
                createMultipleChoiceQuestionDto));

        CreateSurveyDto createSurveyDto = new CreateSurveyDto();
        createSurveyDto.setName(SURVEY_NAME);
        createSurveyDto.setSections(List.of(createSurveySectionDto));
        return createSurveyDto;
    }
}
