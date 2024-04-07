package com.survey.application.services;


import com.survey.application.dtos.OccupationCategoryDto;

import java.util.List;

public interface OccupationCategoryService {
    List<OccupationCategoryDto> getAllOccupationCategories();
}
