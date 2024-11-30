package com.survey.application.services;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import com.survey.domain.models.SurveySendingPolicy;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveySendingPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SurveyParticipationTimeValidationService.class)
public class SurveyParticipationTimeValidationServiceTest {

    @InjectMocks
    private SurveyParticipationTimeValidationServiceImpl surveyParticipationTimeValidationService;

    @Mock
    private SurveySendingPolicyRepository surveySendingPolicyRepository;

    @Mock
    private SurveyParticipationRepository surveyParticipationRepository;

    private UUID surveyId;
    private UUID identityUserId;
    private OffsetDateTime nowUTC;


    @BeforeEach
    void setUp(){
        surveyId = UUID.randomUUID();
        identityUserId = UUID.randomUUID();
        nowUTC = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @Test
    void online_ShouldReturnSurveyFinishDate() {
        OffsetDateTime surveyStartDate = nowUTC.minusMinutes(5);
        OffsetDateTime surveyFinishDate = nowUTC.minusMinutes(1);

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(nowUTC.minusHours(1));
        timeSlot.setFinish(nowUTC.plusHours(1));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId)))
                .thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), any(), any()))
                .thenReturn(false);

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNotNull(result);
        assertEquals(surveyFinishDate, result);

        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verify(surveyParticipationRepository).existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), eq(timeSlot.getStart()), eq(timeSlot.getFinish()));
    }

    @Test
    void online_ShouldReturnSurveyStartDate_WhenSurveyFinishDateIsLateButFitsInLateBuffer() {
        OffsetDateTime surveyStartDate = nowUTC.minusMinutes(5);
        OffsetDateTime surveyFinishDate = nowUTC;

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(nowUTC.minusHours(1));
        timeSlot.setFinish(nowUTC.minusMinutes(4));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId)))
                .thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), any(), any()))
                .thenReturn(false);

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNotNull(result);
        assertEquals(surveyStartDate, result);

        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verify(surveyParticipationRepository).existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), eq(timeSlot.getStart()), eq(timeSlot.getFinish()));
    }

    @Test
    void online_ShouldThrowException_WhenSurveyDoesNotHaveAnyTimeSlots() {
        OffsetDateTime surveyStartDate = nowUTC.minusMinutes(5);
        OffsetDateTime surveyFinishDate = nowUTC;

        when(surveySendingPolicyRepository.findAllBySurveyId(surveyId)).thenReturn(List.of());
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                        identityUserId, surveyId, surveyStartDate, surveyFinishDate));

        assertTrue(exception.getMessage().contains("This survey does not have any currently active time slots."));
    }

    @Test
    void online_ShouldThrowException_WhenSurveyHasTimeSlotsButNoneIsCurrentlyActive() {
        OffsetDateTime surveyStartDate = nowUTC.minusMinutes(5);
        OffsetDateTime surveyFinishDate = nowUTC;


        ArrayList<SurveyParticipationTimeSlot> timeSlots = new ArrayList<>();

        SurveyParticipationTimeSlot timeSlot1 = new SurveyParticipationTimeSlot();
        timeSlot1.setStart(surveyStartDate.minusHours(5));
        timeSlot1.setFinish(surveyFinishDate.minusHours(4));
        timeSlot1.setDeleted(false);
        timeSlots.add(timeSlot1);

        SurveyParticipationTimeSlot timeSlot2 = new SurveyParticipationTimeSlot();
        timeSlot2.setStart(surveyStartDate.plusHours(4));
        timeSlot2.setFinish(surveyFinishDate.plusHours(5));
        timeSlot2.setDeleted(false);
        timeSlots.add(timeSlot2);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(timeSlots);

        when(surveySendingPolicyRepository.findAllBySurveyId(surveyId)).thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                        identityUserId, surveyId, surveyStartDate, surveyFinishDate));

        assertTrue(exception.getMessage().contains("This survey does not have any currently active time slots."));
    }

    @Test
    void online_ShouldThrowException_WhenSurveyHasActiveTimeSlotButResponseFitsInPastTimeSlot() {
        OffsetDateTime surveyStartDate = nowUTC.minusHours(4).minusMinutes(30);
        OffsetDateTime surveyFinishDate = nowUTC.minusHours(4).minusMinutes(25);


        ArrayList<SurveyParticipationTimeSlot> timeSlots = new ArrayList<>();

        SurveyParticipationTimeSlot timeSlotInThePast = new SurveyParticipationTimeSlot();
        timeSlotInThePast.setStart(nowUTC.minusHours(5));
        timeSlotInThePast.setFinish(nowUTC.minusHours(4));
        timeSlotInThePast.setDeleted(false);
        timeSlots.add(timeSlotInThePast);

        SurveyParticipationTimeSlot timeSlotActive = new SurveyParticipationTimeSlot();
        timeSlotActive.setStart(nowUTC.minusHours(1));
        timeSlotActive.setFinish(nowUTC.plusHours(1));
        timeSlotActive.setDeleted(false);
        timeSlots.add(timeSlotActive);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(timeSlots);

        when(surveySendingPolicyRepository.findAllBySurveyId(surveyId)).thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                        identityUserId, surveyId, surveyStartDate, surveyFinishDate));

        assertTrue(exception.getMessage().contains("SurveyStartDate and/or surveyFinishDate do not fit in time slot."));
    }

    @Test
    void online_ShouldThrowException_WhenSurveyHasActiveTimeSlotButResponseFitsInFutureTimeSlot() {
        OffsetDateTime surveyStartDate = nowUTC.plusHours(4).plusMinutes(25);
        OffsetDateTime surveyFinishDate = nowUTC.plusHours(4).plusMinutes(30);


        ArrayList<SurveyParticipationTimeSlot> timeSlots = new ArrayList<>();

        SurveyParticipationTimeSlot timeSlotInTheFuture = new SurveyParticipationTimeSlot();
        timeSlotInTheFuture.setStart(nowUTC.plusHours(4));
        timeSlotInTheFuture.setFinish(nowUTC.plusHours(5));
        timeSlotInTheFuture.setDeleted(false);
        timeSlots.add(timeSlotInTheFuture);

        SurveyParticipationTimeSlot timeSlotActive = new SurveyParticipationTimeSlot();
        timeSlotActive.setStart(nowUTC.minusHours(1));
        timeSlotActive.setFinish(nowUTC.plusHours(1));
        timeSlotActive.setDeleted(false);
        timeSlots.add(timeSlotActive);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(timeSlots);

        when(surveySendingPolicyRepository.findAllBySurveyId(surveyId)).thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                        identityUserId, surveyId, surveyStartDate, surveyFinishDate));

        assertTrue(exception.getMessage().contains("SurveyStartDate and/or surveyFinishDate do not fit in time slot."));
    }

    @Test
    void online_ShouldThrowException_WhenSurveyStartDateIsAfterSurveyFinishDate() {
        OffsetDateTime surveyStartDate = nowUTC;
        OffsetDateTime surveyFinishDate = nowUTC.minusMinutes(5);

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(nowUTC.minusHours(1));
        timeSlot.setFinish(nowUTC.plusHours(1));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(surveyId)).thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                        identityUserId, surveyId, surveyStartDate, surveyFinishDate));

        assertTrue(exception.getMessage().contains("SurveyStartDate and/or surveyFinishDate do not fit in time slot."));
    }

    @Test
    void online_ShouldThrowException_WhenRespondentAlreadyParticipated() {
        OffsetDateTime surveyStartDate = nowUTC.minusMinutes(5);
        OffsetDateTime surveyFinishDate = nowUTC;

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(nowUTC.minusHours(1));
        timeSlot.setFinish(nowUTC.plusHours(1));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId)))
                .thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), any(), any()))
                .thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOnline(
                        identityUserId, surveyId, surveyStartDate, surveyFinishDate));

        assertTrue(exception.getMessage().contains("Respondent already participated in this survey in this time slot."));

        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verify(surveyParticipationRepository).existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), eq(timeSlot.getStart()), eq(timeSlot.getFinish()));
    }



    @Test
    void offline_ShouldReturnSurveyStartDate_WhenSurveyFinishDateIsLateNoMoreThanAllowed() {
        OffsetDateTime surveyStartDate = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);
        OffsetDateTime surveyFinishDate = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(55);

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(surveyStartDate.minusHours(1));
        timeSlot.setFinish(surveyFinishDate.minusMinutes(4));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId)))
                .thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), any(), any()))
                .thenReturn(false);

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOffline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNotNull(result);
        assertEquals(surveyStartDate, result);

        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verify(surveyParticipationRepository).existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), eq(timeSlot.getStart()), eq(timeSlot.getFinish()));
    }

    @Test
    void offline_ShouldReturnSurveyFinishDate_WhenValidPastTimeSlotAndValidDates() {
        OffsetDateTime surveyStartDate = nowUTC.minusHours(3);
        OffsetDateTime surveyFinishDate = nowUTC.minusHours(2).minusMinutes(55);

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(nowUTC.minusHours(4));
        timeSlot.setFinish(nowUTC.minusHours(2));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId))).thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), any(), any()))
                .thenReturn(false);

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOffline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNotNull(result);
        assertEquals(surveyFinishDate, result);

        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verify(surveyParticipationRepository).existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), eq(timeSlot.getStart()), eq(timeSlot.getFinish()));
    }

    @Test
    void offline_ShouldReturnNull_WhenNoMatchingTimeSlot() {
        OffsetDateTime surveyStartDate = nowUTC.minusHours(2);
        OffsetDateTime surveyFinishDate = nowUTC.minusHours(1);

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId))).thenReturn(List.of());

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOffline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNull(result);
        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verifyNoInteractions(surveyParticipationRepository);
    }

    @Test
    void offline_ShouldReturnNull_WhenTimeSlotNotInPast() {
        OffsetDateTime surveyStartDate = nowUTC.plusMinutes(5);
        OffsetDateTime surveyFinishDate = nowUTC.plusMinutes(10);

        SurveyParticipationTimeSlot futureTimeSlot = new SurveyParticipationTimeSlot();
        futureTimeSlot.setStart(nowUTC.plusMinutes(1));
        futureTimeSlot.setFinish(nowUTC.plusMinutes(20));
        futureTimeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(futureTimeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId))).thenReturn(List.of(policy));

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOffline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNull(result);
        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verifyNoInteractions(surveyParticipationRepository);
    }

    @Test
    void offline_ShouldReturnNull_WhenDatesOutsideTimeSlot() {
        OffsetDateTime surveyStartDate = nowUTC.minusHours(6);
        OffsetDateTime surveyFinishDate = nowUTC.minusHours(5).minusMinutes(30);

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(nowUTC.minusHours(4));
        timeSlot.setFinish(nowUTC.minusHours(2));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId))).thenReturn(List.of(policy));

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOffline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNull(result);
        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verifyNoInteractions(surveyParticipationRepository);
    }

    @Test
    void offline_ShouldReturnNull_WhenRespondentAlreadyParticipated() {
        OffsetDateTime surveyStartDate = nowUTC.minusHours(3);
        OffsetDateTime surveyFinishDate = nowUTC.minusHours(2).minusMinutes(55);

        SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot();
        timeSlot.setStart(nowUTC.minusHours(4));
        timeSlot.setFinish(nowUTC.minusHours(2));
        timeSlot.setDeleted(false);

        SurveySendingPolicy policy = new SurveySendingPolicy();
        policy.setTimeSlots(List.of(timeSlot));

        when(surveySendingPolicyRepository.findAllBySurveyId(eq(surveyId))).thenReturn(List.of(policy));
        when(surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), any(), any()))
                .thenReturn(true);

        OffsetDateTime result = surveyParticipationTimeValidationService.getCorrectSurveyParticipationDateTimeOffline(
                identityUserId, surveyId, surveyStartDate, surveyFinishDate);

        assertNull(result);
        verify(surveySendingPolicyRepository).findAllBySurveyId(eq(surveyId));
        verify(surveyParticipationRepository).existsBySurveyIdAndRespondentIdAndDateBetween(
                eq(surveyId), eq(identityUserId), eq(timeSlot.getStart()), eq(timeSlot.getFinish()));
    }
}
