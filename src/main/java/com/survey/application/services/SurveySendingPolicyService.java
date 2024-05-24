package com.survey.application.services;

import com.survey.application.dtos.CreateSurveySendingPolicyDto;
import com.survey.application.dtos.SurveySendingPolicyDto;
import org.apache.coyote.BadRequestException;

import javax.management.BadAttributeValueExpException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import java.util.NoSuchElementException;

public interface SurveySendingPolicyService {
    SurveySendingPolicyDto createSurveySendingPolicy(CreateSurveySendingPolicyDto createSurveySendingPolicyDto) throws InstanceAlreadyExistsException, NoSuchElementException, IllegalArgumentException, BadRequestException, BadAttributeValueExpException, InstanceNotFoundException, InvalidAttributeValueException;
}
