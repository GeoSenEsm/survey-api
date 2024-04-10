package com.survey.application.services;

import com.survey.application.dtos.StressLevelDto;

import java.util.List;

public interface StressLevelService {
    List<StressLevelDto> getAllStressLevels();
}
