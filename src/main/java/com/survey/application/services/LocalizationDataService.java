package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;

import java.util.List;

public interface LocalizationDataService {
    List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtos);
}
