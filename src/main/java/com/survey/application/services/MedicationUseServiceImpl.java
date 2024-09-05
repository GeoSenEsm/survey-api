package com.survey.application.services;

import com.survey.application.dtos.MedicationUseDto;
import com.survey.domain.repository.MedicationUseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class MedicationUseServiceImpl implements MedicationUseService{
    private final MedicationUseRepository repository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public MedicationUseServiceImpl(MedicationUseRepository repository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }


    @Override
    public List<MedicationUseDto> getMedicationUse() {
        String lang = sessionContext.getClientLang();
        return repository.findAll().stream()
                .map(category -> modelMapper.map(category, MedicationUseDto.class).setDisplay(lang != null && lang.equals("pl") ? category.getPolishDisplay() : category.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
