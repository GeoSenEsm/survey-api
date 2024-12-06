package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.models.*;
import com.survey.domain.models.enums.InitialSurveyState;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest(classes = RespondentDataService.class)
class RespondentDataServiceTest {
    private static final String QUESTION_CONTENT = "What is your favorite color?";
    private static final int QUESTION_ORDER = 1;
    private static final UUID QUESTION_ID = UUID.randomUUID();
    private static final String OPTION_CONTENT = "Red";
    private static final int OPTION_ORDER = 1;
    private static final UUID OPTION_ID = UUID.randomUUID();
    private static final String RESPONDENT_USERNAME = "username";
    private static final UUID MOCK_USER_ID = UUID.randomUUID();
    private static final UUID RESPONDENT_DATA_ID = UUID.randomUUID();
    @Mock
    private RespondentDataRepository respondentDataRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private RespondentDataServiceImpl respondentDataService;
    @Mock
    private ClaimsPrincipalService claimsPrincipalService;
    @Mock
    private InitialSurveyRepository initialSurveyRepository;
    @Mock
    private InitialSurveyQuestionRepository initialSurveyQuestionRepository;
    @Mock
    private InitialSurveyOptionRepository initialSurveyOptionRepository;
    @Mock
    private IdentityUserRepository identityUserRepository;
    @Mock
    private RespondentGroupRepository respondentGroupRepository;
    @Mock
    private RespondentToGroupRepository respondentToGroupRepository;
    private CreateRespondentDataDto respondentDataDto;
    private RespondentData respondentData;
    private IdentityUser identityUser;
    private InitialSurvey initialSurvey;
    private InitialSurveyOption mockOption;
    private InitialSurveyQuestion mockQuestion;
    private RespondentGroup mockGroup;

    @BeforeEach
    void setUp() {
        respondentDataDto = createRespondentDataDto();
        identityUser = createIdentityUser();
        initialSurvey = createInitialSurvey();
        mockOption = createMockOption();
        mockQuestion = createMockQuestion(mockOption);
        respondentData = createRespondentData(mockQuestion);
        mockGroup = createMockGroup();
    }
    @Test
    void createRespondent_ShouldSaveAndReturnResponse() throws Exception {
        when(claimsPrincipalService.getCurrentUsernameIfExists()).thenReturn(RESPONDENT_USERNAME);
        when(identityUserRepository.findByUsername(RESPONDENT_USERNAME))
                .thenReturn(Optional.of(identityUser));
        when(claimsPrincipalService.findIdentityUser()).thenReturn(identityUser);
        when(respondentDataRepository.existsByIdentityUserId(MOCK_USER_ID)).thenReturn(false);
        when(initialSurveyRepository.findTopByRowVersionDesc()).thenReturn(Optional.of(initialSurvey));

        when(initialSurveyQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(mockQuestion));
        when(initialSurveyOptionRepository.findById(OPTION_ID))
                .thenReturn(Optional.of(mockOption));

        when(initialSurveyQuestionRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(mockQuestion));
        when(initialSurveyOptionRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(mockOption));

        when(respondentGroupRepository.findByGroupName(any())).thenReturn(mockGroup);

        when(respondentToGroupRepository.saveAllAndFlush(anyList()))
                .thenReturn(Collections.emptyList());

        when(respondentDataRepository.save(any(RespondentData.class))).thenReturn(respondentData);

        Map<String, Object> response = respondentDataService.createRespondent(List.of(respondentDataDto), "testToken");

        verify(respondentDataRepository).save(any(RespondentData.class));
        assertNotNull(response);
        assertEquals(identityUser.getId(), response.get("id"));
        assertEquals(RESPONDENT_USERNAME, response.get("username"));
        assertEquals(OPTION_ID, response.get(QUESTION_CONTENT));
    }

    @Test
    void createRespondent_ShouldThrowInstanceAlreadyExistsException_WhenDataAlreadyExists() {
        when(claimsPrincipalService.getCurrentUsernameIfExists()).thenReturn("testUser");
        when(identityUserRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(identityUser));
        when(respondentDataRepository.existsByIdentityUserId(MOCK_USER_ID)).thenReturn(true);

        assertThrows(NullPointerException.class,
                () -> respondentDataService.createRespondent(List.of(respondentDataDto), "testToken"));
    }

    @Test
    void getAll_ShouldReturnAllRespondents() {
        CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<IdentityUser> mockCriteriaQuery = mock(CriteriaQuery.class);
        Root<IdentityUser> mockRoot = mock(Root.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
        when(mockCriteriaBuilder.createQuery(IdentityUser.class)).thenReturn(mockCriteriaQuery);
        when(mockCriteriaQuery.from(IdentityUser.class)).thenReturn(mockRoot);
        when(mockCriteriaQuery.select(mockRoot)).thenReturn(mockCriteriaQuery);

        TypedQuery<IdentityUser> mockTypedQuery = mock(TypedQuery.class);
        when(mockTypedQuery.getResultList()).thenReturn(List.of(identityUser));
        when(entityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);

        List<Map<String, Object>> response = respondentDataService.getAll(null, null, null, null);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(identityUser.getId(), response.get(0).get("id"));
        assertEquals(RESPONDENT_USERNAME, response.get(0).get("username"));
    }

    @Test
    void getFromUserContext_ShouldReturnRespondentDataForUser() {
        CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<RespondentData> mockCriteriaQuery = mock(CriteriaQuery.class);
        Root<RespondentData> mockRoot = mock(Root.class);

        TypedQuery<RespondentData> mockTypedQuery = mock(TypedQuery.class);
        when(mockTypedQuery.getResultStream()).thenReturn(Stream.of(respondentData));

        when(entityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
        when(mockCriteriaBuilder.createQuery(RespondentData.class)).thenReturn(mockCriteriaQuery);
        when(mockCriteriaQuery.from(RespondentData.class)).thenReturn(mockRoot);
        when(mockCriteriaBuilder.equal(mockRoot.get("identityUserId"), identityUser.getId())).thenReturn(mock(Predicate.class));
        when(entityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);

        when(claimsPrincipalService.findIdentityUser()).thenReturn(identityUser);

        Map<String, Object> response = respondentDataService.getFromUserContext();

        assertNotNull(response);
        assertEquals(identityUser.getId(), response.get("id"));
        assertEquals(RESPONDENT_USERNAME, response.get("username"));
        assertEquals(OPTION_ID, response.get(QUESTION_CONTENT));
    }

    @Test
    void getFromUserContext_ShouldThrowNoSuchElementException_WhenNoDataForUser() {
        TypedQuery<RespondentData> mockTypedQuery = mock(TypedQuery.class);

        when(claimsPrincipalService.findIdentityUser())
                .thenReturn(identityUser);
        when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(mockTypedQuery);
        when(mockTypedQuery.getResultStream()).thenReturn(Stream.empty());

        assertThrows(NullPointerException.class,
                () -> respondentDataService.getFromUserContext());
    }

    @Test
    void getOrCreateInitialSurvey_ShouldReturnExistingSurvey_WhenExists() {
        when(initialSurveyRepository.findTopByRowVersionDesc()).thenReturn(Optional.of(initialSurvey));

        InitialSurvey result = respondentDataService.getOrCreateInitialSurvey();

        assertNotNull(result);
        assertEquals(initialSurvey, result);
        verify(initialSurveyRepository, times(1)).findTopByRowVersionDesc();
        verify(initialSurveyRepository, never()).save(any(InitialSurvey.class));
    }

    @Test
    void getOrCreateInitialSurvey_ShouldCreateNewSurvey_WhenNoneExists() {
        InitialSurvey newSurvey = new InitialSurvey();
        newSurvey.setId(UUID.randomUUID());

        when(initialSurveyRepository.findTopByRowVersionDesc()).thenReturn(Optional.empty());
        when(initialSurveyRepository.save(any(InitialSurvey.class))).thenReturn(newSurvey);

        InitialSurvey result = respondentDataService.getOrCreateInitialSurvey();

        assertNotNull(result);
        assertSame(newSurvey, result);
        verify(initialSurveyRepository, times(1)).findTopByRowVersionDesc();
        verify(initialSurveyRepository, times(1)).save(any(InitialSurvey.class));
    }

    private RespondentGroup createMockGroup() {
        RespondentGroup mockGroup = new RespondentGroup();
        mockGroup.setName("mock group");
        return mockGroup;
    }
    private RespondentData createRespondentData(InitialSurveyQuestion mockQuestion) {
        RespondentData respondentData = new RespondentData();
        respondentData.setIdentityUser(identityUser);
        respondentData.setIdentityUserId(identityUser.getId());
        respondentData.setId(RESPONDENT_DATA_ID);

        RespondentDataQuestion respondentDataQuestion = new RespondentDataQuestion();
        respondentDataQuestion.setRespondentData(respondentData);
        respondentDataQuestion.setQuestion(mockQuestion);

        RespondentDataOption optionSelection = new RespondentDataOption();
        optionSelection.setRespondentDataQuestions(respondentDataQuestion);
        optionSelection.setOption(mockOption);
        respondentDataQuestion.setOptions(Collections.singletonList(optionSelection));

        respondentData.setRespondentDataQuestions(List.of(respondentDataQuestion));
        return respondentData;
    }
    private InitialSurveyQuestion createMockQuestion(InitialSurveyOption mockOption) {
        InitialSurveyQuestion question = new InitialSurveyQuestion();
        question.setId(QUESTION_ID);
        question.setContent(QUESTION_CONTENT);
        question.setOrder(QUESTION_ORDER);
        question.setOptions(List.of(mockOption));
        return question;
    }
    private InitialSurveyOption createMockOption() {
        InitialSurveyOption option = new InitialSurveyOption();
        option.setId(OPTION_ID);
        option.setContent(OPTION_CONTENT);
        option.setOrder(OPTION_ORDER);
        return option;
    }
    private IdentityUser createIdentityUser() {
        IdentityUser user = new IdentityUser();
        user.setId(MOCK_USER_ID);
        user.setUsername(RESPONDENT_USERNAME);
        user.setRole("Respondent");
        return user;
    }
    private CreateRespondentDataDto createRespondentDataDto() {
        CreateRespondentDataDto dto = new CreateRespondentDataDto();
        dto.setQuestionId(QUESTION_ID);
        dto.setOptionId(OPTION_ID);
        return dto;
    }
    private InitialSurvey createInitialSurvey() {
        InitialSurvey initialSurvey = new InitialSurvey();

        InitialSurveyOption option = new InitialSurveyOption();
        option.setOrder(OPTION_ORDER);
        option.setContent(OPTION_CONTENT);
        option.setId(OPTION_ID);

        InitialSurveyQuestion surveyQuestion = new InitialSurveyQuestion();
        surveyQuestion.setOrder(QUESTION_ORDER);
        surveyQuestion.setContent(QUESTION_CONTENT);
        surveyQuestion.setId(QUESTION_ID);
        surveyQuestion.setOptions(List.of(option));

        initialSurvey.setQuestions(List.of(surveyQuestion));
        initialSurvey.setState(InitialSurveyState.published);
        return initialSurvey;
    }
}