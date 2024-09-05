package com.survey.application.services;

import com.survey.application.dtos.HealthConditionDto;
import com.survey.domain.repository.HealthConditionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class HealthConditionServiceImpl implements HealthConditionService {
    private final HealthConditionRepository repository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public HealthConditionServiceImpl(HealthConditionRepository repository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }


    @Override
    public List<HealthConditionDto> getAllHealthConditions() {
        String lang = sessionContext.getClientLang();
        return repository.findAll().stream()
                .map(healthCondition -> modelMapper.map(healthCondition, HealthConditionDto.class).setDisplay(lang != null && lang.equals("pl") ? healthCondition.getPolishDisplay() : healthCondition.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
