package com.survey.application.services;

import com.survey.application.dtos.GreeneryAreaCategoryDto;
import com.survey.domain.models.GreeneryAreaCategory;
import java.util.List;

public interface GreeneryAreaCategoryService {
    List<GreeneryAreaCategoryDto> getAllGreeneryAreaCategories();
}
