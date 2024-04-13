package com.survey.application.services;

import com.survey.application.dtos.QualityOfSleepDto;
import com.survey.domain.repository.QualityOfSleepRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QualityOfSleepServiceImpl implements QualityOfSleepService {
    @Autowired
    private QualityOfSleepRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<QualityOfSleepDto> getAllQualityOfSleep() {

        return repository.findAll().stream().map(qualityOfSleep -> modelMapper.map(qualityOfSleep, QualityOfSleepDto.class)).collect(Collectors.toList());
    }
}
