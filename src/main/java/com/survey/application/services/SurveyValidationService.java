package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateSurveyDto;
import com.survey.domain.models.Survey;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SurveyValidationService {
    void validateShowSections(Survey survey);
    void validateImageChoiceFiles(CreateSurveyDto survey, List<MultipartFile> files);
}
