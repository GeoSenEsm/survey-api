package com.survey.application.services;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import com.survey.domain.models.SurveySendingPolicy;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.domain.repository.SurveySendingPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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
        SurveyParticipationTimeSlot currentTimeSlot = validateAndFindTimeSlot(surveyId, surveyStartDate, identityUserId);

        if (!isWithinTimeSlot(currentTimeSlot, surveyStartDate, surveyFinishDate)) {
            throw new IllegalArgumentException("Start and finish dates do not fit within the time slot.");
        }

        return calculateParticipationDate(currentTimeSlot, surveyStartDate, surveyFinishDate);
    }

    @Override
    public OffsetDateTime getCorrectSurveyParticipationDateTimeOffline(UUID identityUserId, UUID surveyId, OffsetDateTime surveyStartDate, OffsetDateTime surveyFinishDate) {
        SurveyParticipationTimeSlot timeSlot = findTimeSlotForStartDate(surveyId, surveyStartDate);

        if (timeSlot == null ||
            hasExistingParticipation(surveyId, identityUserId, timeSlot) ||
                !isWithinTimeSlot(timeSlot, surveyStartDate, surveyFinishDate)){
            return null;
        }

        return calculateParticipationDate(timeSlot, surveyStartDate, surveyFinishDate);
    }



    private SurveyParticipationTimeSlot validateAndFindTimeSlot(UUID surveyId, OffsetDateTime surveyStartDate, UUID identityUserId) {
        SurveyParticipationTimeSlot timeSlot = findTimeSlotForStartDate(surveyId, surveyStartDate);

        if (timeSlot == null) {
            throw new IllegalArgumentException("No active time slot found for the given start date: " + surveyStartDate);
        }

        if (hasExistingParticipation(surveyId, identityUserId, timeSlot)) {
            throw new IllegalArgumentException("Respondent already participated in this survey during the time slot: " +
                    timeSlot.getStart() + " - " + timeSlot.getFinish());
        }

        return timeSlot;
    }

    private SurveyParticipationTimeSlot findTimeSlotForStartDate(UUID surveyId, OffsetDateTime surveyStartDate) {
        if (surveyStartDate.isAfter(OffsetDateTime.now())) {
            return null;
        }

        List<SurveySendingPolicy> policies = surveySendingPolicyRepository.findAllBySurveyId(surveyId);
        return policies.stream()
                .flatMap(policy -> policy.getTimeSlots().stream())
                .filter(slot -> !slot.isDeleted() &&
                        surveyStartDate.isAfter(slot.getStart()) &&
                        surveyStartDate.isBefore(slot.getFinish()))
                .findFirst()
                .orElse(null);
    }

    private boolean hasExistingParticipation(UUID surveyId, UUID respondentId, SurveyParticipationTimeSlot timeSlot) {
        return surveyParticipationRepository.existsBySurveyIdAndRespondentIdAndDateBetween(surveyId, respondentId, timeSlot.getStart(), timeSlot.getFinish());
    }

    private boolean isWithinTimeSlot(SurveyParticipationTimeSlot timeSlot, OffsetDateTime startDate, OffsetDateTime finishDate) {
        return startDate.isBefore(finishDate) &&
                startDate.isAfter(timeSlot.getStart()) &&
                startDate.isBefore(timeSlot.getFinish()) &&
                finishDate.isBefore(timeSlot.getFinish().plusMinutes(ALLOWED_LATE_MINUTES));
    }

    private OffsetDateTime calculateParticipationDate(SurveyParticipationTimeSlot timeSlot, OffsetDateTime startDate, OffsetDateTime finishDate) {
        return finishDate.isBefore(timeSlot.getFinish()) ? finishDate : startDate;
    }

}
