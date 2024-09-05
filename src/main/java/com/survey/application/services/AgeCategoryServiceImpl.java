package com.survey.application.services;

import com.survey.application.dtos.AgeCategoryDto;
import com.survey.domain.repository.AgeCategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class AgeCategoryServiceImpl implements AgeCategoryService{
    private final AgeCategoryRepository repository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public AgeCategoryServiceImpl(AgeCategoryRepository repository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }

    @Override
    public List<AgeCategoryDto> getAllAgeCategories() {
        String lang = sessionContext.getClientLang();
        return repository.findAll().stream()
                .map(category -> modelMapper.map(category, AgeCategoryDto.class).setDisplay(lang != null && lang.equals("pl") ? category.getPolishDisplay() : category.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
