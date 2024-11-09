package com.survey.application.services;

import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;
import com.survey.domain.models.ResearchArea;
import com.survey.domain.repository.ResearchAreaRepository;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ResearchAreaServiceImpl implements ResearchAreaService {
    private final ResearchAreaRepository researchAreaRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;

    public ResearchAreaServiceImpl(ResearchAreaRepository researchAreaRepository, ModelMapper modelMapper, EntityManager entityManager) {
        this.researchAreaRepository = researchAreaRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ResponseResearchAreaDto saveResearchArea(ResearchAreaDto researchAreaDto) {
        ResearchArea researchArea = modelMapper.map(researchAreaDto, ResearchArea.class);
        researchArea = researchAreaRepository.saveAndFlush(researchArea);
        entityManager.refresh(researchArea);
        return modelMapper.map(researchArea, ResponseResearchAreaDto.class);
    }

    @Override
    public ResponseResearchAreaDto getResearchArea() {
        Optional<ResearchArea> researchAreaOptional = researchAreaRepository.findFirstByOrderByIdAsc();
        return modelMapper.map(researchAreaOptional, ResponseResearchAreaDto.class);
    }

    @Override
    public boolean deleteResearchArea() {
        Optional<ResearchArea> researchAreaOptional = researchAreaRepository.findFirstByOrderByIdAsc();
        if (researchAreaOptional.isPresent()) {
            researchAreaRepository.delete(researchAreaOptional.get());
            return true;
        }
        return false;
    }
}
