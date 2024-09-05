package com.survey.application.services;

import com.survey.application.dtos.StressLevelDto;
import com.survey.domain.repository.StressLevelRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class StressLevelServiceImpl implements StressLevelService {
    private final StressLevelRepository stressLevelRepository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public StressLevelServiceImpl(StressLevelRepository stressLevelRepository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.stressLevelRepository = stressLevelRepository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }


    @Override
    public List<StressLevelDto> getAllStressLevels() {
        String lang = sessionContext.getClientLang();
        return stressLevelRepository.findAll().stream()
                .map(category -> modelMapper.map(category, StressLevelDto.class).setDisplay(lang != null && lang.equals("pl") ? category.getPolishDisplay() : category.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
