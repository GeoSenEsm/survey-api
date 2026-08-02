package com.survey.application.services;

import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SurveyParticipationTimeSlot;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveySendingPolicyRepository;
import com.survey.domain.models.SurveySendingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Validates survey start/finish against study wall-clock slots interpreted in
 * the respondent's timezone. Clients may upload timestamps in the respondent's
 * local offset; comparisons are done on UTC instants after converting each
 * slot's LocalDateTime face with that zone.
 */
@Service
public class SurveyParticipationTimeValidationServiceImpl implements SurveyParticipationTimeValidationService{

    private static final Logger LOGGER = Logger.getLogger(SurveyParticipationTimeValidationServiceImpl.class.getName());

    private static final int ALLOWED_LATE_MINUTES = 5;

    private final SurveySendingPolicyRepository surveySendingPolicyRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final IdentityUserRepository identityUserRepository;
    private final RespondentTimeZoneService respondentTimeZoneService;

    @Autowired
    public SurveyParticipationTimeValidationServiceImpl(
            SurveySendingPolicyRepository surveySendingPolicyRepository,
            SurveyParticipationRepository surveyParticipationRepository,
            IdentityUserRepository identityUserRepository,
            RespondentTimeZoneService respondentTimeZoneService) {
        this.surveySendingPolicyRepository = surveySendingPolicyRepository;
        this.surveyParticipationRepository = surveyParticipationRepository;
        this.identityUserRepository = identityUserRepository;
        this.respondentTimeZoneService = respondentTimeZoneService;
    }


    @Override
    public OffsetDateTime getCorrectSurveyParticipationDateTimeOnline(UUID identityUserId, UUID surveyId, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate) {
        ZoneId zone = zoneFor(identityUserId);
        SurveyParticipationTimeSlot timeSlot = getCurrentlyActiveTimesSlot(surveyId, zone);
        if (timeSlot == null){
            throw new IllegalArgumentException("This survey does not have any currently active time slots.");
        }

        if (!areSurveyStartAndFinishDatesWithinGivenTimeSlot(timeSlot, zone, surveyStartDate, surveyFinishDate)){
            throw new IllegalArgumentException("SurveyStartDate and/or surveyFinishDate do not fit in time slot.");
        }

        if (!isSurveyFinishDateBeforeCurrentTime(surveyFinishDate)){
            LOGGER.severe("Survey finish date is from the future. " + surveyFinishDate + " vs current date " +  respondentTimeZoneService.nowUtc());
        }

        if (hasRespondentParticipatedInSurveyInSpecifiedTimeSlot(surveyId, identityUserId, timeSlot, zone)){
            throw new IllegalArgumentException("Respondent already participated in this survey in this time slot.");
        }

        return getFinalSurveyParticipationDate(timeSlot, zone, surveyStartDate, surveyFinishDate);
    }

    @Override
    public OffsetDateTime getCorrectSurveyParticipationDateTimeOffline(UUID identityUserId, UUID surveyId, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate) {
        ZoneId zone = zoneFor(identityUserId);
        SurveyParticipationTimeSlot timeSlot = findTimeSlotForSurveyStartDate(surveyId, zone, surveyStartDate);

        if (timeSlot == null ||
                !isTimeslotStartInThePast(timeSlot, zone) ||
                !areSurveyStartAndFinishDatesWithinGivenTimeSlot(timeSlot, zone, surveyStartDate, surveyFinishDate) ||
                hasRespondentParticipatedInSurveyInSpecifiedTimeSlot(surveyId, identityUserId, timeSlot, zone)) {
            return null;
        }
        return getFinalSurveyParticipationDate(timeSlot, zone, surveyStartDate, surveyFinishDate);
    }


    private ZoneId zoneFor(UUID identityUserId) {
        IdentityUser user = identityUserRepository.findById(identityUserId)
                .orElseThrow(() -> new NoSuchElementException("Respondent not found: " + identityUserId));
        return respondentTimeZoneService.resolveZoneId(user);
    }

    private boolean isSurveyFinishDateBeforeCurrentTime(OffsetDateTime surveyFinishDate){
        return respondentTimeZoneService.toUtc(surveyFinishDate).isBefore(respondentTimeZoneService.nowUtc());
    }

    private SurveyParticipationTimeSlot findTimeSlotForSurveyStartDate(UUID surveyId, ZoneId zone, OffsetDateTime surveyStartDate){
        OffsetDateTime startUtc = respondentTimeZoneService.toUtc(surveyStartDate);
        List<SurveySendingPolicy> sendingPolicies = surveySendingPolicyRepository.findAllBySurveyId(surveyId);

        return sendingPolicies.stream()
                .flatMap(policy -> policy.getTimeSlots().stream())
                .filter(slot -> !slot.isDeleted())
                .filter(slot -> {
                    OffsetDateTime slotStart = respondentTimeZoneService.slotStartUtc(slot, zone);
                    OffsetDateTime slotFinish = respondentTimeZoneService.slotFinishUtc(slot, zone);
                    return startUtc.isAfter(slotStart) && startUtc.isBefore(slotFinish);
                })
                .findFirst()
                .orElse(null);
    }

    private SurveyParticipationTimeSlot getCurrentlyActiveTimesSlot(UUID surveyId, ZoneId zone){
        OffsetDateTime now = respondentTimeZoneService.nowUtc();
        List<SurveySendingPolicy> sendingPolicies = surveySendingPolicyRepository.findAllBySurveyId(surveyId);

        return sendingPolicies.stream()
                .flatMap(policy -> policy.getTimeSlots().stream())
                .filter(slot -> !slot.isDeleted())
                .filter(slot -> {
                    OffsetDateTime slotStart = respondentTimeZoneService.slotStartUtc(slot, zone);
                    OffsetDateTime slotFinish = respondentTimeZoneService.slotFinishUtc(slot, zone)
                            .plusMinutes(ALLOWED_LATE_MINUTES);
                    return now.isAfter(slotStart) && now.isBefore(slotFinish);
                })
                .findFirst()
                .orElse(null);
    }

    private boolean isTimeslotStartInThePast(SurveyParticipationTimeSlot timeSlot, ZoneId zone){
        return respondentTimeZoneService.slotStartUtc(timeSlot, zone)
                .isBefore(respondentTimeZoneService.nowUtc());
    }

    private boolean areSurveyStartAndFinishDatesWithinGivenTimeSlot(
            SurveyParticipationTimeSlot timeSlot,
            ZoneId zone,
            OffsetDateTime surveyStartDate,
            OffsetDateTime surveyFinishDate){
        OffsetDateTime startUtc = respondentTimeZoneService.toUtc(surveyStartDate);
        OffsetDateTime finishUtc = respondentTimeZoneService.toUtc(surveyFinishDate);
        OffsetDateTime slotStart = respondentTimeZoneService.slotStartUtc(timeSlot, zone);
        OffsetDateTime slotFinish = respondentTimeZoneService.slotFinishUtc(timeSlot, zone)
                .plusMinutes(ALLOWED_LATE_MINUTES);
        return startUtc.isBefore(finishUtc)
                && startUtc.isAfter(slotStart)
                && finishUtc.isBefore(slotFinish);
    }

    private boolean hasRespondentParticipatedInSurveyInSpecifiedTimeSlot(
            UUID surveyId,
            UUID respondentId,
            SurveyParticipationTimeSlot timeSlot,
            ZoneId zone){
        OffsetDateTime slotStart = respondentTimeZoneService.slotStartUtc(timeSlot, zone);
        OffsetDateTime slotFinish = respondentTimeZoneService.slotFinishUtc(timeSlot, zone);
        return surveyParticipationRepository.existsBySurveyIdAndIdentityUserIdAndDateBetween(
                surveyId, respondentId, slotStart, slotFinish);
    }

    private OffsetDateTime getFinalSurveyParticipationDate(
            SurveyParticipationTimeSlot timeSlot,
            ZoneId zone,
            OffsetDateTime surveyStartDate,
            OffsetDateTime surveyFinishDate){
        OffsetDateTime startUtc = respondentTimeZoneService.toUtc(surveyStartDate);
        OffsetDateTime finishUtc = respondentTimeZoneService.toUtc(surveyFinishDate);
        OffsetDateTime slotFinish = respondentTimeZoneService.slotFinishUtc(timeSlot, zone);
        return finishUtc.isBefore(slotFinish) ? finishUtc : startUtc;
    }

}
