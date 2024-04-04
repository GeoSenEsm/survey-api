package com.survey.application.services;

import com.survey.application.dtos.GreeneryAreaCategoryDto;
import com.survey.domain.repository.GreeneryAreaCategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class GreeneryAreaCategoryServiceImpl implements GreeneryAreaCategoryService{
    @Autowired
    private GreeneryAreaCategoryRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<GreeneryAreaCategoryDto> getAllGreeneryAreaCategories() {

        return repository.findAll().stream()
                .map(category -> modelMapper.map(category, GreeneryAreaCategoryDto.class))
                .collect(Collectors.toList());
    }
}
