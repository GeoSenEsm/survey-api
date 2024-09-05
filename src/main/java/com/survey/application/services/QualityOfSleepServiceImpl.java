package com.survey.application.services;

import com.survey.application.dtos.QualityOfSleepDto;
import com.survey.domain.repository.QualityOfSleepRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScope
public class QualityOfSleepServiceImpl implements QualityOfSleepService {
    private final QualityOfSleepRepository repository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public QualityOfSleepServiceImpl(QualityOfSleepRepository repository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }


    @Override
    public List<QualityOfSleepDto> getAllQualityOfSleep() {
        String lang = sessionContext.getClientLang();
        return repository.findAll().stream().map(qualityOfSleep ->
                modelMapper.map(qualityOfSleep, QualityOfSleepDto.class).setDisplay(lang != null && lang.equals("pl") ? qualityOfSleep.getPolishDisplay() : qualityOfSleep.getEnglishDisplay()))
                .collect(Collectors.toList());
    }
}
