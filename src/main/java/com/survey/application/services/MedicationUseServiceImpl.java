package com.survey.application.services;

import com.survey.application.dtos.MedicationUseDto;
import com.survey.domain.repository.MedicationUseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationUseServiceImpl implements MedicationUseService{
    @Autowired
    private MedicationUseRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<MedicationUseDto> getMedicationUse() {

        return repository.findAll().stream()
                .map(category -> modelMapper.map(category, MedicationUseDto.class))
                .collect(Collectors.toList());
    }
}
