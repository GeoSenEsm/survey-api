package com.survey.application.services;

import com.survey.application.dtos.CreateSurveySendingPolicyDto;
import com.survey.application.dtos.SurveySendingPolicyDto;

import javax.management.InstanceAlreadyExistsException;
import java.util.NoSuchElementException;

public interface SurveySendingPolicyService {
    SurveySendingPolicyDto addSurveySendingPolicy(CreateSurveySendingPolicyDto createSurveySendingPolicyDto) throws InstanceAlreadyExistsException, NoSuchElementException,  IllegalArgumentException;
}
