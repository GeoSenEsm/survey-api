package com.survey.application.services;

import com.survey.application.dtos.LifeSatisfactionDto;
import com.survey.domain.repository.LifeSatisfactionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class LifeSatisfactionServiceImpl implements LifeSatisfactionService{
    private final LifeSatisfactionRepository lifeSatisfactionRepository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public LifeSatisfactionServiceImpl(LifeSatisfactionRepository lifeSatisfactionRepository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.lifeSatisfactionRepository = lifeSatisfactionRepository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }


    @Override
    public List<LifeSatisfactionDto> getAllLifeSatisfactionValues() {
        String lang = sessionContext.getClientLang();

        return lifeSatisfactionRepository.findAll().stream()
                .map(category -> modelMapper.map(category, LifeSatisfactionDto.class).setDisplay(lang != null && lang.equals("pl") ? category.getPolishDisplay() : category.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
