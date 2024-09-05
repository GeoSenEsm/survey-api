package com.survey.application.services;

import com.survey.application.dtos.OccupationCategoryDto;
import com.survey.domain.repository.GreeneryAreaCategoryRepository;
import com.survey.domain.repository.OccupationCategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class OccupationCategoryServiceImpl implements OccupationCategoryService{
    private final OccupationCategoryRepository repository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public OccupationCategoryServiceImpl(OccupationCategoryRepository repository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }


    @Override
    public List<OccupationCategoryDto> getAllOccupationCategories() {
        String lang = sessionContext.getClientLang();
        return repository.findAll().stream()
                .map(category -> modelMapper.map(category, OccupationCategoryDto.class).setDisplay(lang != null && lang.equals("pl") ? category.getPolishDisplay() : category.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
