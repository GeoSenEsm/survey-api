package com.survey.application.services;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import com.survey.domain.models.SurveySendingPolicy;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveySendingPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class SurveyParticipationTimeValidationServiceImpl implements SurveyParticipationTimeValidationService{

    private static final int ALLOWED_LATE_MINUTES = 5;

    private final SurveySendingPolicyRepository surveySendingPolicyRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;

    @Autowired
    public SurveyParticipationTimeValidationServiceImpl(SurveySendingPolicyRepository surveySendingPolicyRepository, SurveyParticipationRepository surveyParticipationRepository) {
        this.surveySendingPolicyRepository = surveySendingPolicyRepository;
        this.surveyParticipationRepository = surveyParticipationRepository;
    }


    @Override
    public OffsetDateTime getCorrectSurveyParticipationDateTimeOnline(UUID identityUserId, UUID surveyId, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate) {
        SurveyParticipationTimeSlot timeSlot = getCurrentlyActiveTimesSlot(surveyId);
        if (timeSlot == null){
            throw new IllegalArgumentException("This survey does not have any currently active time slots.");
        }

        if (!areSurveyStartAndFinishDatesWithinGivenTimeSlot(timeSlot, surveyStartDate, surveyFinishDate)){
            throw new IllegalArgumentException("SurveyStartDate and/or surveyFinishDate do not fit in time slot.");
        }

        if (!isSurveyFinishDateBeforeCurrentTime(surveyFinishDate)){
            throw new IllegalArgumentException("Survey finish date is from the future.");
        }

        if (hasRespondentParticipatedInSurveyInSpecifiedTimeSlot(surveyId, identityUserId, timeSlot)){
            throw new IllegalArgumentException("Respondent already participated in this survey in this time slot.");
        }

        return getFinalSurveyParticipationDate(timeSlot, surveyStartDate, surveyFinishDate);
    }

    @Override
    public OffsetDateTime getCorrectSurveyParticipationDateTimeOffline(UUID identityUserId, UUID surveyId, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate) {
        SurveyParticipationTimeSlot timeSlot = findTimeSlotForSurveyStartDate(surveyId, surveyStartDate);

        if (timeSlot == null ||
                !isTimeslotStartInThePast(timeSlot) ||
                !areSurveyStartAndFinishDatesWithinGivenTimeSlot(timeSlot, surveyStartDate, surveyFinishDate) ||
                !isSurveyFinishDateBeforeCurrentTime(surveyFinishDate) ||
                hasRespondentParticipatedInSurveyInSpecifiedTimeSlot(surveyId, identityUserId, timeSlot)) {
            return null;
        }
        return getFinalSurveyParticipationDate(timeSlot, surveyStartDate, surveyFinishDate);
    }


    private boolean isSurveyFinishDateBeforeCurrentTime(OffsetDateTime surveyFinishDate){
        return surveyFinishDate.isBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private SurveyParticipationTimeSlot findTimeSlotForSurveyStartDate(UUID surveyId, OffsetDateTime surveyStartDate){
        List<SurveySendingPolicy> sendingPolicies = surveySendingPolicyRepository.findAllBySurveyId(surveyId);

        return sendingPolicies.stream()
                .flatMap(policy -> policy.getTimeSlots().stream())
                .filter(slot -> !slot.isDeleted() &&
                        surveyStartDate.isAfter(slot.getStart()) &&
                        surveyStartDate.isBefore(slot.getFinish()))
                .findFirst()
                .orElse(null);
    }

    private SurveyParticipationTimeSlot getCurrentlyActiveTimesSlot(UUID surveyId){
        List<SurveySendingPolicy> sendingPolicies = surveySendingPolicyRepository.findAllBySurveyId(surveyId);

        return sendingPolicies.stream()
                .flatMap(policy -> policy.getTimeSlots().stream())
                .filter(slot -> !slot.isDeleted() &&
                        OffsetDateTime.now(ZoneOffset.UTC).isAfter(slot.getStart()) &&
                        OffsetDateTime.now(ZoneOffset.UTC).isBefore(slot.getFinish().plusMinutes(ALLOWED_LATE_MINUTES)))
                .findFirst()
                .orElse(null);
    }

    private boolean isTimeslotStartInThePast(SurveyParticipationTimeSlot timeSlot){
        return timeSlot.getStart().isBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private boolean areSurveyStartAndFinishDatesWithinGivenTimeSlot(SurveyParticipationTimeSlot timeSlot, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate){
        return surveyStartDate.isBefore(surveyFinishDate) &&
                surveyStartDate.isAfter(timeSlot.getStart()) &&
                surveyFinishDate.isBefore(timeSlot.getFinish().plusMinutes(ALLOWED_LATE_MINUTES));
    }

    private boolean hasRespondentParticipatedInSurveyInSpecifiedTimeSlot(UUID surveyId, UUID respondentId, SurveyParticipationTimeSlot timeSlot){
        return surveyParticipationRepository.existsBySurveyIdAndIdentityUserIdAndDateBetween(surveyId, respondentId, timeSlot.getStart(), timeSlot.getFinish());
    }

    private OffsetDateTime getFinalSurveyParticipationDate(SurveyParticipationTimeSlot timeSlot, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate){
        return surveyFinishDate.isBefore(timeSlot.getFinish()) ? surveyFinishDate : surveyStartDate;
    }

}
