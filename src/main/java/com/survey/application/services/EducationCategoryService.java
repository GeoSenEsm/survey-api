package com.survey.application.services;

import com.survey.application.dtos.EducationCategoryDto;

import java.util.List;

public interface EducationCategoryService {
    List<EducationCategoryDto> getAllEducationCategories();
}
