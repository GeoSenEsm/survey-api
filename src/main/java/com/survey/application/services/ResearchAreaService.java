package com.survey.application.services;

import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface ResearchAreaService {
    List<ResponseResearchAreaDto> saveResearchArea(List<ResearchAreaDto> researchAreaDto) throws BadRequestException;
    List<ResponseResearchAreaDto> getResearchArea();
    boolean deleteResearchArea();
}
