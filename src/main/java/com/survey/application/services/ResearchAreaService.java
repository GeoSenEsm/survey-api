package com.survey.application.services;

import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;

import java.util.List;

public interface ResearchAreaService {
    List<ResponseResearchAreaDto> saveResearchArea(List<ResearchAreaDto> researchAreaDto);
    List<ResponseResearchAreaDto> getResearchArea();
    boolean deleteResearchArea();
}
