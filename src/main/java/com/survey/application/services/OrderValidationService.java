package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveySectionDto;

import java.util.List;

public interface OrderValidationService {
    boolean validateOrders(List<CreateSurveySectionDto> surveySections);
}
