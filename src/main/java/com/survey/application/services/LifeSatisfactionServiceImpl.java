package com.survey.application.services;

import com.survey.application.dtos.LifeSatisfactionDto;
import com.survey.domain.repository.LifeSatisfactionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LifeSatisfactionServiceImpl implements LifeSatisfactionService{
    @Autowired
    private LifeSatisfactionRepository lifeSatisfactionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<LifeSatisfactionDto> getAllLifeSatisfactionValues() {

        return lifeSatisfactionRepository.findAll().stream()
                .map(category -> modelMapper.map(category, LifeSatisfactionDto.class))
                .collect(Collectors.toList());
    }
}
