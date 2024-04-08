package com.survey.application.services;

import com.survey.application.dtos.StressLevelDto;
import com.survey.domain.repository.StressLevelRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StressLevelServiceImpl implements StressLevelService {
    @Autowired
    private StressLevelRepository stressLevelRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<StressLevelDto> getAllStressLevels() {
        return stressLevelRepository.findAll().stream()
                .map(category -> modelMapper.map(category, StressLevelDto.class))
                .collect(Collectors.toList());
    }
}
