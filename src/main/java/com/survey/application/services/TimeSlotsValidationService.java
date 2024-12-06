package com.survey.application.services;

import com.survey.application.dtos.CreateSurveySendingPolicyDto;

import javax.management.InvalidAttributeValueException;

public interface TimeSlotsValidationService {
    void validateTimeSlots(CreateSurveySendingPolicyDto dto) throws InvalidAttributeValueException;
}
