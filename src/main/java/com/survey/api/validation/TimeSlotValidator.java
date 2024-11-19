package com.survey.api.validation;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import com.survey.domain.repository.SurveyParticipationTimeSlotRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.UUID;

public class TimeSlotValidator implements ConstraintValidator<ValidTimeSlotIds, List<UUID>> {

    private final SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository;

    public TimeSlotValidator(SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository) {
        this.surveyParticipationTimeSlotRepository = surveyParticipationTimeSlotRepository;
    }

    @Override
    public boolean isValid(List<UUID> ids, ConstraintValidatorContext constraintValidatorContext) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        long existingCount = surveyParticipationTimeSlotRepository.countByIdIn(ids);
        return ids.size() == existingCount;
    }
}
