package com.survey.application.services;

import com.survey.application.dtos.CreateSurveySendingPolicyDto;
import com.survey.application.dtos.SurveyParticipationTimeStartFinishDto;
import org.springframework.stereotype.Component;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TimeSlotsValidationServiceImpl implements TimeSlotsValidationService{

    @Override
    public void validateTimeSlots(CreateSurveySendingPolicyDto dto) throws InvalidAttributeValueException {
        List<String> validationErrors = new ArrayList<>();

        List<SurveyParticipationTimeStartFinishDto> timeSlots = dto.getSurveyParticipationTimeSlots();

        for (SurveyParticipationTimeStartFinishDto slot : timeSlots) {
            if (slot.getStart() == null || slot.getFinish() == null || slot.getFinish().isBefore(slot.getStart())) {
                validationErrors.add("Invalid time slot: " + slot.getStart() + " " + slot.getFinish());
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new InvalidAttributeValueException(String.join("\n", validationErrors));
        }
    }
}
