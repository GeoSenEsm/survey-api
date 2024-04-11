package com.survey.application.services;

import java.util.List;

import com.survey.application.dtos.HealthConditionDto;

public interface HealthConditionService {
    List<HealthConditionDto> getAllHealthConditions();
}
