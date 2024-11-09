package com.survey.application.services;

import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;

public interface ResearchAreaService {
    ResponseResearchAreaDto saveResearchArea(ResearchAreaDto researchAreaDto);
    ResponseResearchAreaDto getResearchArea();
    boolean deleteResearchArea();
}
