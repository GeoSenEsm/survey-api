package com.survey.application.services;

import com.survey.application.dtos.OccupationCategoryDto;
import com.survey.domain.repository.GreeneryAreaCategoryRepository;
import com.survey.domain.repository.OccupationCategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OccupationCategoryServiceImpl implements OccupationCategoryService{
    @Autowired
    private OccupationCategoryRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<OccupationCategoryDto> getAllOccupationCategories() {

        return repository.findAll().stream()
                .map(category -> modelMapper.map(category, OccupationCategoryDto.class))
                .collect(Collectors.toList());
    }
}
