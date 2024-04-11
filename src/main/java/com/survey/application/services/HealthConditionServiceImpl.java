package com.survey.application.services;

import com.survey.application.dtos.HealthConditionDto;
import com.survey.domain.repository.HealthConditionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthConditionServiceImpl implements HealthConditionService {
    @Autowired
    private HealthConditionRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<HealthConditionDto> getAllHealthConditions() {

        return repository.findAll().stream()
                .map(healthCondition -> modelMapper.map(healthCondition, HealthConditionDto.class))
                .collect(Collectors.toList());
    }
}
