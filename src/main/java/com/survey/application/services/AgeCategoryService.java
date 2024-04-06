package com.survey.application.services;

import com.survey.application.dtos.AgeCategoryDto;


import java.util.List;

public interface AgeCategoryService {
    List<AgeCategoryDto> getAllAgeCategories();
}
