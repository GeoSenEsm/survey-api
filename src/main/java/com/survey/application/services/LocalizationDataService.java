package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LocalizationDataService {
    List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtoList, String token);
    List<ResponseLocalizationDto> getLocalizationData(OffsetDateTime from, OffsetDateTime to, UUID respondentId, UUID surveyId, boolean outsideResearchArea);
}
