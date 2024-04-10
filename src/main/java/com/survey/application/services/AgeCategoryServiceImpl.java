package com.survey.application.services;

import com.survey.application.dtos.AgeCategoryDto;
import com.survey.domain.repository.AgeCategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgeCategoryServiceImpl implements AgeCategoryService{
    @Autowired
    private AgeCategoryRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<AgeCategoryDto> getAllAgeCategories() {
        return repository.findAll().stream()
                .map(category -> modelMapper.map(category, AgeCategoryDto.class))
                .collect(Collectors.toList());
    }
}
