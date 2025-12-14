package com.survey.application.services;

import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;
import com.survey.domain.models.ResearchArea;
import com.survey.domain.repository.ResearchAreaRepository;
import jakarta.persistence.EntityManager;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ResearchAreaServiceImpl implements ResearchAreaService {
    private final ResearchAreaRepository researchAreaRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    private static final String PROCEDURE_CALL_RECALCULATE_ALL_POINTS = "EXEC RecalculateOutsideResearchArea";
    private static final String PROCEDURE_CALL_UPDATE_STORED_POLYGON = "EXEC UpdateStoredPolygon";

    public ResearchAreaServiceImpl(ResearchAreaRepository researchAreaRepository, ModelMapper modelMapper, EntityManager entityManager, JdbcTemplate jdbcTemplate) {
        this.researchAreaRepository = researchAreaRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public List<ResponseResearchAreaDto> saveResearchArea(List<ResearchAreaDto> researchAreaDtoList) throws BadRequestException {
        if (researchAreaDtoList == null || researchAreaDtoList.size() < 3) {
            throw new BadRequestException("The research area list must contain at least 3 elements.");
        }
        if (researchAreaDtoList.size() > 250) {
            throw new BadRequestException("The research area list can contain max. 250 elements");
        }

        researchAreaRepository.deleteAll();
        researchAreaRepository.flush();

        AtomicInteger atomicInteger = new AtomicInteger(0);
        List<ResearchArea> researchAreas = researchAreaDtoList.stream()
                .map(dto -> {
                    ResearchArea researchArea = modelMapper.map(dto, ResearchArea.class);
                    researchArea.setOrder(atomicInteger.getAndIncrement());
                    return researchArea;
                }).sorted(Comparator.comparingInt(ResearchArea::getOrder)).collect(Collectors.toList());

        List<ResearchArea> savedResearchAreas = researchAreaRepository.saveAllAndFlush(researchAreas);

        savedResearchAreas.forEach(entityManager::refresh);

        jdbcTemplate.update(PROCEDURE_CALL_UPDATE_STORED_POLYGON);
        jdbcTemplate.update(PROCEDURE_CALL_RECALCULATE_ALL_POINTS);

        return savedResearchAreas.stream()
                .map(researchArea -> modelMapper.map(researchArea, ResponseResearchAreaDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseResearchAreaDto> getResearchArea() {
        List<ResearchArea> researchAreas = researchAreaRepository.findAll();

        return researchAreas.stream()
                .sorted(Comparator.comparingInt(ResearchArea::getOrder))
                .map(researchArea -> modelMapper.map(researchArea, ResponseResearchAreaDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteResearchArea() {
        long count = researchAreaRepository.count();
        if (count > 0) {
            researchAreaRepository.deleteAll();
            jdbcTemplate.update(PROCEDURE_CALL_UPDATE_STORED_POLYGON);
            jdbcTemplate.update(PROCEDURE_CALL_RECALCULATE_ALL_POINTS);
            return true;
        }
        return false;
    }
}
