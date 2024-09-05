package com.survey.application.services;

import com.survey.application.dtos.EducationCategoryDto;
import com.survey.domain.repository.EducationCategoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class EducationCategoryServiceImpl implements EducationCategoryService {
    private final EducationCategoryRepository educationCategoryRepository;
    private final ModelMapper mapper;
    private final SessionContext sessionContext;

    public EducationCategoryServiceImpl(EducationCategoryRepository educationCategoryRepository, ModelMapper mapper, SessionContext sessionContext) {
        this.educationCategoryRepository = educationCategoryRepository;
        this.mapper = mapper;
        this.sessionContext = sessionContext;
    }

    @Override
    public List<EducationCategoryDto> getAllEducationCategories() {
        String lang = sessionContext.getClientLang();
        return educationCategoryRepository.findAll().stream()
                .map(category -> mapper.map(category, EducationCategoryDto.class).setDisplay(lang != null && lang.equals("pl") ? category.getPolishDisplay() : category.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
