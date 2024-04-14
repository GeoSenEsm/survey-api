package com.survey.application.services;

import com.survey.application.dtos.EducationCategoryDto;
import com.survey.domain.repository.EducationCategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EducationCategoryServiceImpl implements EducationCategoryService {
    private final EducationCategoryRepository educationCategoryRepository;
    private final ModelMapper mapper;

    public EducationCategoryServiceImpl(EducationCategoryRepository educationCategoryRepository, ModelMapper mapper) {
        this.educationCategoryRepository = educationCategoryRepository;
        this.mapper = mapper;
    }

    @Override
    public List<EducationCategoryDto> getAllEducationCategories() {
        return educationCategoryRepository.findAll().stream()
                .map(category -> mapper.map(category, EducationCategoryDto.class))
                .collect(Collectors.toList());
    }
}
