package com.survey.application.services;

import com.survey.application.dtos.SensorProfileTemplateDto;
import com.survey.application.dtos.SensorTypeDtoOut;

import java.util.List;

public interface SensorProfileTemplateService {
    List<SensorProfileTemplateDto> listTemplates();

    SensorTypeDtoOut install(String templateCode);
}
