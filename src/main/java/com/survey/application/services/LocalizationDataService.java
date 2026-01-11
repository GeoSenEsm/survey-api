package com.survey.application.services;

import com.survey.application.dtos.LocalizationDataDto;
import com.survey.application.dtos.ResponseLocalizationDto;

import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LocalizationDataService {
    List<ResponseLocalizationDto> saveLocalizationData(List<LocalizationDataDto> localizationDataDtoList);
    List<ResponseLocalizationDto> getLocalizationData(OffsetDateTime from, OffsetDateTime to, UUID identityUserId, UUID surveyId, Boolean outsideResearchArea);
    List<ResponseLocalizationDto> getLocalizationDataBatch(OffsetDateTime from, OffsetDateTime to, UUID identityUserId, UUID surveyId, Boolean outsideResearchArea, int offset, int limit);
    void streamLocalizationData(OutputStream outputStream, OffsetDateTime from, OffsetDateTime to, UUID identityUserId, UUID surveyId, Boolean outsideResearchArea) throws Exception;
}
